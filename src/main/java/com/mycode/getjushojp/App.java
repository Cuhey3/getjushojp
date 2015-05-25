package com.mycode.getjushojp;

import java.io.IOException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class App {

    public static void main(String[] args) throws IOException, Exception {
        Main main = new Main();
        main.addRouteBuilder(new MyRouteBuilder());
        System.out.println("更新を開始しました。");
        main.run();
    }
}

class MyRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        onException(Throwable.class)
                .process(messageProcessor("エラーが発生しました。更新は完了していません。"))
                .delay(3000)
                .process(exitProcessor());

        from("timer:foo?repeatCount=1")
                .process(messageProcessor("ダウンロードしています…"))
                .to("jetty:http://jusyo.jp/downloads/new/sqlite/sqlite_zenkoku.zip")
                .process(messageProcessor("展開しています…"))
                .unmarshal().zipFile().to("file:program")
                .process(messageProcessor("更新が完了しました。間もなくプログラムを終了します。"))
                .delay(3000)
                .process(exitProcessor());
    }

    public Processor exitProcessor() {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                System.exit(0);
            }
        };
    }

    public Processor messageProcessor(final String message) {
        return new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println(message);
            }
        };
    }
}
