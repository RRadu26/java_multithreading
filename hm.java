import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.Scanner;

class productThread implements Runnable {
    int no;
    String code;

    File outproducts;
    productThread(int no, File outproducts,String code) {
        this.no = no;
        this.code = code;
        this.outproducts = outproducts;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(Tema2.productS));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileWriter fw = null;

        try {
            fw = new FileWriter(outproducts, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int currentno = 0;
        while(currentno < no) {
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(line == null)
                break;
            synchronized (this) {
                if (line.startsWith(code)) {
                    try {
                        fw.write(line + ",shipped\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class ordersThread implements Runnable {
    BufferedReader reader;
    File outorders;
    File outproducts;
    ordersThread(File outorders, File outproducts, BufferedReader reader, int i) {
        this.reader = reader;
        this.outorders = outorders;
        this.outproducts = outproducts;
    }

    @Override
    public synchronized void run() {
        String line = null;
        while (true) {
            synchronized (this) {
                FileWriter fw = null;
                try {
                    fw = new FileWriter(outorders, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line == null)
                    break;
                String[] parts = line.split(",");
                String code = parts[0];
                int number = Integer.parseInt(parts[1]);
                if(number == 0)
                    continue;
                Thread angajat = new Thread(new productThread(number, outproducts, code));
                angajat.start();
                try {
                    angajat.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    fw.write(line + ",shipped\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

public class hm {
    public static String productS;
    public static void main(String[] args) throws IOException {
        File outproducts = new File("order_products_out.txt");
        File outorders = new File("orders_out.txt");

        productS = new String(args[0] + "/order_products.txt");
        boolean result = Files.deleteIfExists(outorders.toPath());
        result = Files.deleteIfExists((outproducts.toPath()));

        BufferedReader reader = new BufferedReader(new FileReader(args[0] + "/orders.txt"));

            int number_of_threads = Integer.parseInt(args[1]);
            Thread[] threads = new Thread[number_of_threads];
            for (int i = 0; i < number_of_threads; i++)
                threads[i] = new Thread(new ordersThread(outorders, outproducts, reader, i));
            for (int i = 0; i < number_of_threads; i++)
                threads[i].start();
            for (int i = 0; i < number_of_threads; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
