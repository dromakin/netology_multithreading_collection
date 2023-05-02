package org.dromakin;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

public class Main {

    private static final int TEXT_SIZE = 100_000;
    private static final int COUNT_TEXTS = 10_000;
    private static final int QUEUE_SIZE = 100;

    private static final BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static final BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static final BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(QUEUE_SIZE);


    private static class Task implements Runnable {

        private final BlockingQueue<String> queue;
        private final char symbol;

        public Task(BlockingQueue<String> queue, char symbol) {
            this.queue = queue;
            this.symbol = symbol;
        }

        @Override
        public void run() {
            int maxCount = 0;
            String maxString = "";
            for (int i = 0; i < COUNT_TEXTS; i++) {
                try {
                    String text = this.queue.take();
                    int count = (int) IntStream.range(0, text.length()).filter(j -> text.charAt(j) == this.symbol).count();
                    if (count > maxCount) {
                        maxString = text;
                        maxCount = count;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.printf("Текст с максимальным количеством (%d) символов %s: %s\n", maxCount, this.symbol, maxString);
//            System.out.printf("Текст с максимальным количеством (%d) символов %s\n", maxCount, this.symbol);

        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
    public static void main(String[] args) throws InterruptedException {
        Thread textGenerator = new Thread(() -> {
            for (int i = 0; i < COUNT_TEXTS; i++) {
                String text = generateText("abc", TEXT_SIZE);
                queue1.add(text);
                queue2.add(text);
                queue3.add(text);
            }
        });


        Thread counterA = new Thread(new Task(queue1, 'a'));
        Thread counterB = new Thread(new Task(queue2, 'b'));
        Thread counterC = new Thread(new Task(queue3, 'c'));

        textGenerator.start();
        counterA.start();
        counterB.start();
        counterC.start();

        textGenerator.join();
        counterA.join();
        counterB.join();
        counterC.join();

    }
}