package mobanker.testdeviceparametersdemo

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.os.StatFs
import android.telephony.*
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.WindowManager


/**
 *  zhanglong
 *  2018/4/11
 */
object IntenetUtil {

    //没有网络连接
    val NETWORN_NONE = 0
    //wifi连接
    val NETWORN_WIFI = 1
    //手机网络数据连接类型
    val NETWORN_2G = 2
    val NETWORN_3G = 3
    val NETWORN_4G = 4
    val NETWORN_MOBILE = 5

    /**
     * 获取当前网络连接类型
     * @param context
     * @return
     */
    fun getNetworkState(context: Context): Int {
        //获取系统的网络服务
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager ?: return NETWORN_NONE

        //如果当前没有网络

        //获取当前网络类型，如果为空，返回无网络
        val activeNetInfo = connManager.activeNetworkInfo
        if (activeNetInfo == null || !activeNetInfo.isAvailable) {
            return NETWORN_NONE
        }

        // 判断是不是连接的是不是wifi
        val wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (null != wifiInfo) {
            val state = wifiInfo.state
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_WIFI
                }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        if (null != networkInfo) {
            val state = networkInfo.state
            val strSubTypeName = networkInfo.subtypeName
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    when (activeNetInfo.subtype) {
                    //如果是2g类型
                        TelephonyManager.NETWORK_TYPE_GPRS // 联通2g
                            , TelephonyManager.NETWORK_TYPE_CDMA // 电信2g
                            , TelephonyManager.NETWORK_TYPE_EDGE // 移动2g
                            , TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return NETWORN_2G
                    //如果是3g类型
                        TelephonyManager.NETWORK_TYPE_EVDO_A // 电信3g
                            , TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> return NETWORN_3G
                    //如果是4g类型
                        TelephonyManager.NETWORK_TYPE_LTE -> return NETWORN_4G
                        else ->
                            //中国移动 联通 电信 三种3G制式
                            return if (strSubTypeName.equals("TD-SCDMA", ignoreCase = true) || strSubTypeName.equals("WCDMA", ignoreCase = true) || strSubTypeName.equals("CDMA2000", ignoreCase = true)) {
                                NETWORN_3G
                            } else {
                                NETWORN_MOBILE
                            }
                    }
                }
        }
        return NETWORN_NONE
    }
}

object DeviceUtil {
    fun getDeviceMetrics(context: Context): CharSequence {
        val mWindowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        mWindowManager.defaultDisplay.getMetrics(metrics)
        val result = StringBuilder()
        result.append("with:").append(metrics.widthPixels).append("height:").append(metrics.heightPixels)
        return result
    }
}

object MemoryUtil {
    /**
     * Get Memory Info
     */
    fun getMemoryInfo(context: Context): String {
        // 获得一个磁盘状态对象
        val stat = StatFs(Environment.getExternalStorageDirectory().path)

        val blockSize = stat.blockSize.toLong()   // 获得一个扇区的大小

        val totalBlocks = stat.blockCount.toLong()    // 获得扇区的总数

        val availableBlocks = stat.availableBlocks.toLong()   // 获得可用的扇区数量

        // 总空间
        val totalMemory = Formatter.formatFileSize(context, totalBlocks * blockSize)
        // 可用空间
        val availableMemory = Formatter.formatFileSize(context, availableBlocks * blockSize)

        return "总空间: $totalMemory\n可用空间: $availableMemory"
    }

    /**
     * RAM
     */
     fun getAvailMemory(context: Context): String {// 获取android当前可用内存大小

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        //mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(context, mi.availMem)// 将获取的内存大小规格化
    }

    fun getTotalRam(context: Context): String {//GB
//        val path = "/proc/meminfo"
//        var firstLine: String? = null
//        var totalRam = 0
//        try {
//            val fileReader = FileReader(path)
//            val br = BufferedReader(fileReader, 8192)
//            firstLine = br.readLine().split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
//            br.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        if (firstLine != null) {
//            totalRam = Math.ceil((java.lang.Float.valueOf(firstLine)!! / (1024 * 1024)).toDouble()).toInt()
//        }
//
//        return totalRam.toString() + "GB"//返回1GB/2GB/3GB/4GB
        val memInfo = ActivityManager.MemoryInfo()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return  am.getMemoryInfo(memInfo).toString()

    }
}

object OperatorInfo {
    //获取基站信息
    @SuppressLint("MissingPermission")
    public fun getTowerInfo(context: Context): List<String> {
        var mcc = -1;
        var mnc = -1;
        var lac = -1;
        var cellId = -1;
        var rssi = -1;
        var tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var operator = tm.networkOperator;
        mcc = Integer.parseInt(operator.substring(0, 3));
        var list = ArrayList<String>();
        var infos = tm.allCellInfo;
        for (info: CellInfo in infos) {
            if (info is CellInfoCdma) {
                var cellInfoCdma = info;
                var cellIdentityCdma = cellInfoCdma . getCellIdentity ();
                mnc = cellIdentityCdma.getSystemId();
                lac = cellIdentityCdma.getNetworkId();
                cellId = cellIdentityCdma.getBasestationId();
                var cellSignalStrengthCdma = cellInfoCdma . getCellSignalStrength ();
                rssi = cellSignalStrengthCdma.getCdmaDbm();
            } else if (info is CellInfoGsm) {
                var cellInfoGsm = info;
                var cellIdentityGsm = cellInfoGsm . getCellIdentity ();
                mnc = cellIdentityGsm.getMnc();
                lac = cellIdentityGsm.getLac();
                cellId = cellIdentityGsm.getCid();
                var cellSignalStrengthGsm = cellInfoGsm . getCellSignalStrength ();
                rssi = cellSignalStrengthGsm.getDbm();
            } else if (info is CellInfoLte) {
                var cellInfoLte = info;
                var  cellIdentityLte = cellInfoLte .cellIdentity
                mnc = cellIdentityLte.getMnc();
                lac = cellIdentityLte.getTac();
                cellId = cellIdentityLte.getCi();
                var cellSignalStrengthLte:CellSignalStrengthLte = cellInfoLte . getCellSignalStrength ();
                rssi = cellSignalStrengthLte.getDbm();
            } else if (info is CellInfoWcdma) {
                var cellInfoWcdma = info;
                var cellIdentityWcdma: CellIdentityWcdma
                var cellSignalStrengthWcdma: CellSignalStrengthWcdma
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    mnc = cellIdentityWcdma.getMnc();
                    lac = cellIdentityWcdma.getLac();
                    cellId = cellIdentityWcdma.getCid();
                    cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                    rssi = cellSignalStrengthWcdma.getDbm();
                }
            } else {
                return list;
            }
            val tower = StringBuilder().append(mcc).append("#").append(mnc).append("#").append(lac).append("#").append(cellId).append("#").append(rssi)
            list.add(tower.toString())
        }
        return list;
    }
    @SuppressLint("MissingPermission")
    fun getNeighborCellInfo(context: Context){
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val infos = telephonyManager.neighboringCellInfo
        val sb = StringBuffer("总数 : " + infos.size + "\n")
        for (info1 in infos) { // 根据邻区总数进行循环
            sb.append(" LAC : " + info1.lac) // 取出当前邻区的LAC
            sb.append(" CID : " + info1.cid) // 取出当前邻区的CID
            sb.append(" BSSS : " + (-113 + 2 * info1.rssi) + "\n") // 获取邻区基站信号强度
        }
    }
}
