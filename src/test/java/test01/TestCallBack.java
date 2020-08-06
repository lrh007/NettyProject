package test01;

import java.util.concurrent.TimeUnit;

/**测试回调方法
 * @Author lrh 2020/8/6 14:22
 */
public class TestCallBack {

    class Data{
        private int n,m;

        public Data(int n, int m) {
            this.n = n;
            this.m = m;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "n=" + n +
                    ", m=" + m +
                    '}';
        }
    }


    interface FetcherCallback{
        void onData(Data data)throws Exception;
        void onError(Throwable cause);
    }


    interface Fetcher{
        void fetcherData(FetcherCallback callback);
    }


    static class MyFetcher implements Fetcher{
        private Data data;
        @Override
        public void fetcherData(final FetcherCallback callback) {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                            callback.onData(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                System.out.println("异步执行onData");
            } catch (Exception e) {
                callback.onError(e);
            }
        }
    }


    static class Work{
        public void doWork(){
            Fetcher fetcher = new MyFetcher();
            fetcher.fetcherData(new FetcherCallback() {
                @Override
                public void onData(Data data) throws Exception {
                    System.out.println("接收的数据："+data);
                }

                @Override
                public void onError(Throwable cause) {
                    System.out.println("错误信息："+cause.getMessage());
                }
            });
        }
    }

    public static void main(String[] args) {
        new Work().doWork();

    }


}
