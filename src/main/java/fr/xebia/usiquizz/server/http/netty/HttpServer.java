/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package fr.xebia.usiquizz.server.http.netty;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class HttpServer implements Daemon {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static int firstPort = 80;

    private final ChannelGroup allChannels = new DefaultChannelGroup("quizz-server");

    private HttpServerPipelineFactory server;

    public static void main(String[] args) throws Exception {
        if(args.length > 0){
            firstPort = Integer.parseInt(args[0]);
        }
        HttpServer server = new HttpServer();
        server.start();
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    @Override
    public void start() throws Exception {
        // Si args[0] present. on considere que c'est le nombre de I/O server worker
        int nbThread = 4;
        int nbListeningPort = 1;

        ThreadFactory bossThreadFactory = new ThreadFactory() {

            private int i = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("BossExec #1-" + i++);
                return thread;
            }
        };

        ThreadFactory ioThreadFactory = new ThreadFactory() {

            private int i = 1;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("BossExec #1-" + i++);
                return thread;
            }
        };
        // Configure the server.
        //ExecutorService bossExec = Executors.newFixedThreadPool(20, threadFactory);
        //ExecutorService bossExec = Executors.newCachedThreadPool();
        //ExecutorService ioExec = Executors.newCachedThreadPool();
        ExecutorService bossExec = new OrderedMemoryAwareThreadPoolExecutor(nbListeningPort, 400000000, 2000000000, 60, TimeUnit.SECONDS, bossThreadFactory);
        ExecutorService ioExec = new OrderedMemoryAwareThreadPoolExecutor(nbThread, 400000000, 2000000000, 60, TimeUnit.SECONDS, ioThreadFactory);


        logger.info("start server with {} listning port", nbListeningPort);
        logger.info("Start with {} thread worker", nbThread);
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(bossExec, ioExec, nbThread));


        // Set up the event pipeline factory.
        server = new HttpServerPipelineFactory(bossExec);
        logger.info("Pipeline initialized");
        bootstrap.setPipelineFactory(server);

        // Bind and start to accept incoming connections.

        // A priori beaucoup de pb de connection reset by peer sous macos sans ces options
        bootstrap.setOption("child.tcpNoDelay", true);
        //bootstrap.setOption("child.keepAlive", false);
        bootstrap.setOption("backlog", 1000);
        Channel c = bootstrap.bind(new InetSocketAddress(firstPort));
        allChannels.add(c);
        logger.info("Ready for quizz");
    }

    @Override
    public void stop() throws Exception {
        // call shutdown to server
        server.shutdown();
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
