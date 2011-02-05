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
package fr.xebia.usi.quizz.web.netty;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class HttpServer {
    public static void main(String[] args) {
        // Configure the server.
        //MemoryAwareThreadPoolExecutor bossExec = new MemoryAwareThreadPoolExecutor(1, 1000000, 10000000);
        //MemoryAwareThreadPoolExecutor ioExec = new MemoryAwareThreadPoolExecutor(2, 1000000, 10000000);
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8080));
    }
}
