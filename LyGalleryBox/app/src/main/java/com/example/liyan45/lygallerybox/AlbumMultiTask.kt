package com.example.liyan45.lygallerybox

import android.os.AsyncTask
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by liyan45 on 17/4/10.
 */
abstract class AlbumMultiTask<Params, Progress, Result> : AsyncTask<Params, Progress, Result>()  {

    companion object {
        val CPU_COUNT : Int = Runtime.getRuntime().availableProcessors()
        val CORE_POOL_SIZE : Int = CPU_COUNT + 1
        val MAX_POOL_SIZE : Int = CPU_COUNT + 3
        val KEEP_ALIVE : Long = 10
        val sPoolWorkQueue : BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>(1024)
        var mTHREAD_POOL_EXECUTOR : Executor? = null

        var sThreadFactory : ThreadFactory = object : ThreadFactory {
            val mCount : AtomicInteger = AtomicInteger(1)
            override fun newThread(r: Runnable?): Thread {
                val thread = Thread(r, "lyAlbumMultiTask # " + mCount.getAndIncrement())
                thread.priority = Thread.MIN_PRIORITY
                return thread
            }
        }




        fun initThreadPool() {
            mTHREAD_POOL_EXECUTOR = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE,
                    TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory)
        }
    }

    fun executeDependSDK(vararg params : Params) {
        if (mTHREAD_POOL_EXECUTOR == null) {
            initThreadPool()
        }
        super.executeOnExecutor(mTHREAD_POOL_EXECUTOR, *params)
    }
}


