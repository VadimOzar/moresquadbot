package com.moresquad;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

public class BotMain {

    private SlackSession session;
    private SlackChannel generalChannel;
    private static CloseableHttpClient client;

    private static String[] maxAve = {"макс", "шмель", "лендел", "дурак", "долбойоб", "гей", "пидр", "max"};

    public static void main(String[] args) throws IOException {

        client = HttpClientBuilder.create().build();

        Properties pro = new Properties();
        pro.load(BotMain.class.getResourceAsStream("/project.properties"));

        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(pro.getProperty("slack.bot.api"));
        session.connect();
        SlackChannel general = session.findChannelByName("general"); //make sure bot is a member of the channel.
        session.addMessagePostedListener((event, session1) -> {
            if (!event.getSender().isBot()) {
                String message = event.getMessageContent().toLowerCase();

                if (containsMax(message))
                    session1.sendMessage(event.getChannel(), "Аве максім!!");
                else if (event.getMessageContent().contains("слава україні")) {
                    session1.sendMessage(event.getChannel(), "Максу слава :pride:");
                } else if (event.getSender().getUserName().equals("lendyelushka")) {
                    session1.sendMessage(event.getChannel(), "Я краще б і не сказав");
                } else if (event.getMessageContent().contains("буха")) {
                    session1.sendMessage(event.getChannel(), "Хтось сказав бухати???? Я за!! І Макс к стате тоже :beers: :beer: :beers:");
                }

                if (event.getMessageContent().contains("обзови ")) {
                    try {
                        session1.sendMessage(event.getChannel(), callSomebody(event.getMessageContent().replace("обзови ", "")));
                    } catch (IOException e) {
                        session1.sendMessage(event.getChannel(), "чет не вышло :cry:");
                    }
                }
            }
        });
    }

    private static String callSomebody(String name) throws IOException {
        HttpGet get = new HttpGet("https://damn.ru/?name=" + name + "&sex=m");
        CloseableHttpResponse res = client.execute(get);
        StringWriter writer = new StringWriter();
        IOUtils.copy(res.getEntity().getContent(), writer, Charset.defaultCharset());
        return getMessageFromResp(writer.toString());
    }

    private static String getMessageFromResp(String s) {
        int start = s.indexOf("<div class=\"damn\" data-id=\"");
        int end = s.indexOf("</div", start + 1);
        String withSpan = s.substring(start + 36, end);
        withSpan = withSpan.replace("<span class=\"name\">", "");
        withSpan = withSpan.replace("&mdash; ", "");
        return withSpan.replace("</span>", "");
    }


    private static boolean containsMax(String message) {
        return Arrays.stream(maxAve).map(message::contains).filter(aBoolean -> aBoolean).findAny().orElse(false);
    }

}
