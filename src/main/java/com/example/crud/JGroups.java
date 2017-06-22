package com.example.crud;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jgroups.*;
import org.jgroups.blocks.locking.LockService;

public class JGroups {
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    /* this variable indicates whether I have become the master or I'm just a client*/
    public volatile AtomicBoolean becomeMaster = new AtomicBoolean(false);
    /* The address of the server if we are a client or of ourself if we are
     * server */
    public String serverAddress;
    /* A channel on which to acquire a lock, so that only one can become server */
    private JChannel lockChannel;
    /* A shared channel ffor communication between client and master*/
    private JChannel communicationChannel;
    private LockService lockService;
    /* A thread which tries to acquire a lock */
    private Thread acquiringThread;
    /* A thread which listens for the server ip which may change */
    private Thread listeningThread;
    /* A thread which lists the status and initializes the acquiring thread*/
    private Thread statusThread;
    private String name;
    /* If we pass from being a client to being a server we must stop the listening
     * thread however we cannot call listeningThread.stop() but instead we change
     * the stopListening boolean to true */
    private boolean stopListening = false;
    /* This lock communicates I have finally become either master or client so
     * the serverAddress and becomeMaster variables are correctly set */
    public final Object finishedLock = new Object();

    public static void main(String[] args) throws Exception {
        //System.setProperty("jgroups.udp.mcast_addr", "127.0.0.1");
        Thread.currentThread().setName("MyMainThread");
        Random rand = new Random();

        JGroups master = new JGroups("Node" + rand.nextInt(10));

        master.lockChannel = new JChannel(JGroups.class.getClassLoader().getResource(
                "jgroups-l2-cache-udp-largecluster.xml"));
        master.lockChannel.connect("lock-channel");

        master.communicationChannel = new JChannel(
                JGroups.class.getClassLoader().getResource("jgroups-l2-cache-udp-largecluster.xml"));
        master.communicationChannel.connect("communication-channel");

        master.lockService = new LockService(master.lockChannel);
        master.startStatusPrinterThread();
    }

    public JGroups(String name) {
        this.name = name;
    }

    public JGroups() {
        try {
            Thread.currentThread().setName("MyMainThread");
            Random rand = new Random();

            this.name = ("Node" + rand.nextInt(10));

            lockChannel = new JChannel(JGroups.class.getClassLoader().getResource("/resource/udp.xml"));
            lockChannel.connect("lock-channel");

            communicationChannel = new JChannel(JGroups.class.getClassLoader().getResource("/resource/udp.xml"));
            communicationChannel.connect("communication-channel");

            lockService = new LockService(lockChannel);
            startStatusPrinterThread();
        }
        catch (Exception ex) {
            log.error(ex.getStackTrace().toString());
        }
    }

    public void startAcquiringThread() {
        acquiringThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    //if you have become Master send your ip every now and then
                    if (becomeMaster.get()) {
                        try {
                            StringBuffer buffer = new StringBuffer("serverip " + serverAddress);
                            communicationChannel.send(new Message(null, buffer));
                        }
                        catch (Exception ex) {
                            log.error(ex.getStackTrace().toString());
                        }
                    } else {
                        try {
                            Thread.currentThread().setName(name + "AcquiringThread");
                            Lock lock = lockService.getLock("serverLock");
                            if (lock.tryLock(4, TimeUnit.SECONDS)) {
                                becomeMaster.set(true);
                                stopListening = true;
                                /* Now that I'm server I must find out my own ip address on which to listen */
                                Enumeration<NetworkInterface> networkInterfaces;
                                try {
                                    networkInterfaces = NetworkInterface.getNetworkInterfaces();
                                    for (NetworkInterface netint : Collections.list(networkInterfaces)) {
                                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                                            if (isIPAddress(inetAddress.getHostAddress())
                                                    && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                                serverAddress = inetAddress.getHostAddress();
                                            }
                                        }
                                    }
                                    /* I notify to the rest of the program I have correctly initialized 
                                     * becomeMaster and serverAddress */
                                    synchronized (finishedLock) {
                                        finishedLock.notify();
                                    }
                                }
                                catch (Exception ex) {
                                    log.error(ex.getStackTrace().toString());
                                    System.exit(0);
                                }
                                log.info(Thread.currentThread().getName()
                                        + ": I acquired lock! will become master! my ip is " + serverAddress);
                            } else {
                                becomeMaster.set(false);
                                stopListening = false;
                                if (listeningThread == null || !listeningThread.isAlive()) {
                                    if (!stopListening) {
                                        //??? this codnition might be useless
                                        startListeningThread();
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        sleep(5000L);
                    }
                    catch (InterruptedException ex) {
                        log.error(ex.getStackTrace().toString());
                    }
                }
            }
        };
        acquiringThread.setDaemon(true);
        acquiringThread.start();
    }

    public void startListeningThread() {
        listeningThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.currentThread().setName(name + "ListeningThread");
                        communicationChannel.setReceiver(new ReceiverAdapter() {
                            @Override
                            public void receive(Message msg) {
                                if (msg.getObject() != null) {
                                    String leaderServerAddress = (msg.getObject().toString().substring(9));
                                    if (isIPAddress(leaderServerAddress)) {
                                        serverAddress = leaderServerAddress;
                                        log.info(name + " Master server has ip" + serverAddress);
                                        /* I notify to the rest of the program I have correctly initialized 
                                         * becomeMaster and serverAddress */
                                        synchronized (finishedLock) {
                                            finishedLock.notify();
                                        }
                                    } else {
                                        log.info(name + ": discarded message " + msg.getObject().toString());
                                    }
                                }
                            }
                        });
                        sleep(10000L);
                        if (stopListening) {
                            return;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    private void startStatusPrinterThread() {
        statusThread = new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName(name + "StatusPrinterThread");
                startAcquiringThread();
                while (true) {
                    try {
                        if (becomeMaster.get()) {
                            log.info(name + " startStatusPrinterThread(): I am happily a Master!");
                        } else {
                            if (!acquiringThread.isAlive()) {
                                startAcquiringThread();
                            }
                        }
                        sleep(5000L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        statusThread.setDaemon(true);
        statusThread.start();
    }

    private static boolean isIPAddress(String str) {
        Pattern ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        return ipPattern.matcher(str).matches();
    }
}
