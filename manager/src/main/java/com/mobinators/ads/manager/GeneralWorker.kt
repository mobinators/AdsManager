package com.mobinators.ads.manager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class GeneralWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        return Result.success()
    }
}