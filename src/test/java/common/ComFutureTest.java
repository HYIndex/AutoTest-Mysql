package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ComFutureTest {
    public static void main(String [] args) {
        final int i = 0;
        while (true) {
            CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                return String.format("%s : %s\n", fmt.format(new Date()), i);
            });
            CompletableFuture<Void> f1 = f.thenAccept(System.out::println);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
