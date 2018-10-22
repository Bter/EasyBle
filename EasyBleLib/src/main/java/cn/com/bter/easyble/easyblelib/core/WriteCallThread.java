package cn.com.bter.easyble.easyblelib.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteCallThread {
    private ExecutorService singleThread;

    private WriteCallThread(){
        singleThread = Executors.newSingleThreadExecutor();
    }

    public static WriteCallThread getInstance(){
        return Holder.INSTANCE;
    }

    public void excuteTask(Runnable task){
        if(task != null) {
            singleThread.execute(task);
        }
    }

    private static class Holder{
        private static final WriteCallThread INSTANCE = new WriteCallThread();
    }
}
