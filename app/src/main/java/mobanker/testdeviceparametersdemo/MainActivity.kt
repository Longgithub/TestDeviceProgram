package mobanker.testdeviceparametersdemo

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import net.vidageek.mirror.dsl.Mirror
import java.io.*
import java.net.NetworkInterface
import java.util.*










class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showDeviceInfo()
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun showDeviceInfo() {
        var result = StringBuilder()
        result.append("UUID:").append(UUID.randomUUID()).append("\n")
        result.append("WLAN MAC:").append(getMac()).append("\n")
        result.append("Bluetooth MAC:").append(getBtAddressMac()).append("\n")
        result.append("Time Zone :").append(getCurrentTimeZone()).append("\n")
        result.append("NetworType:'").append(IntenetUtil.getNetworkState(this)).append("\n")
        result.append("DisplayMetrics:").append(DeviceUtil.getDeviceMetrics(this)).append("\n")
        result.append("Memory Info:").append(MemoryUtil.getTotalRam(this)).append("\n").append(MemoryUtil.getAvailMemory(this))
        //需要插卡
//        result.append("基站信息").append(OperatorInfo.getTowerInfo(this)).append("\n")
//        result.append("附近基站：").append(OperatorInfo.getNeighborCellInfo(this)).append("\n")

        result.append("制造商:").append(android.os.Build.MANUFACTURER).append("\n")
        result.append("设备版本包信息:").append(android.os.Build.DISPLAY).append("\n")
        result.append("设备厂商信息:").append(android.os.Build.PRODUCT).append("\n")
        result.append("设备硬件信息:").append(Build.HARDWARE).append("\n")
        result.append("设备主机地址:").append(Build.HOST).append("\n")
        result.append("设备版本号").append(Build.ID).append("\n")
        result.append("设备序列号").append(Build.SERIAL).append("\n")
        result.append("设备版本类型").append(Build.TYPE).append("\n")
        result.append("国家码").append(getCountryZipCode(this)).append("\n")
        result.append("获取的ISO国家码:").append(getSimISO()).append("\n")
        //6.0y以上
//        result.append("Battery Status:").append(getBatteryStatus()).append("\n")
        result.append("SD卡：").append(MemoryUtil.getMemoryInfo(this)).append("\n")
        result.append("是否已经Root:").append(isRooted()).append("\n")
        result.append("当前WIFI信息").append(getWifiInfo()).append("\n")
        var tele=getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        result.append("移动网络操作码:").append(tele.networkOperator).append("|").append(tele.networkOperatorName).append("|").append(tele.simOperator)
        result.append("CPU型号:").append(Build.CPU_ABI).append("\n")
        result.append("CPU最大频率:").append(getMaxCpuFreq()).append("\n")
//        result.append("USB制造商:").append(Build.MANUFACTURER).append("\n")
        result.append("硬件识别码:").append(Build.FINGERPRINT).append("\n")
        //需要插卡
//        result.append("SIM卡状态:").append(tele.simState).append("\n")
//        result.append("SIM卡序列号:").append(tele.simSerialNumber).append("\n")
        result.append("ANDROID_ID:").append(Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID)).append("\n")
        val pm = packageManager
        result.append("是否支持移动网络：").append(pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_GSM)||pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA)).append("\n")
        result.append("是否有WIFI:").append(pm.hasSystemFeature(PackageManager.FEATURE_WIFI)).append("\n")
        result.append("是否有gps").append(pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)).append("\n")
        result.append("是否支持电话功能").append(pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)).append("\n")
        result.append("是否支持蓝牙").append(pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)).append("\n")
        uuidTv.text = result
    }

    fun getMacAddr(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res = StringBuilder()
                for (b in macBytes) {
                    res.append(String.format("%02X:", b))
                }
                if (res.length > 0) {
                    res.deleteCharAt(res.length - 1)
                }

                return res.toString()
            }
        } catch (ex: Exception) {
            Log.w("MacAddr", "exception during retrieving MAC address: " + ex.message)
        }

        return "02:00:00:00:00:00"
    }

    /**
     * 获取手机的MAC地址
     *
     * @return
     */
    fun getMac(): String? {
        var str: String? = ""
        var macSerial: String? = ""
        try {
            val pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ")
            val ir = InputStreamReader(pp.inputStream)
            val input = LineNumberReader(ir)

            while (null != str) {
                str = input.readLine()
                if (str != null) {
                    macSerial = str.trim { it <= ' ' }// 去空格
                    break
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (macSerial == null || "" == macSerial) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17)
            } catch (e: Exception) {
                e.printStackTrace()

            }

        }
        return macSerial
    }

    @Throws(Exception::class)
    fun loadFileAsString(fileName: String): String {
        val reader = FileReader(fileName)
        val text = loadReaderAsString(reader)
        reader.close()
        return text
    }

    @Throws(Exception::class)
    fun loadReaderAsString(reader: Reader): String {
        val builder = StringBuilder()
        val buffer = CharArray(4096)
        var readLength = reader.read(buffer)
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength)
            readLength = reader.read(buffer)
        }
        return builder.toString()
    }


    fun getBtAddressMac(): String? {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        var bluetoothService = Mirror().on(adapter).get().field("mService");
        if (bluetoothService == null) {
            return ""
        }
        var address = Mirror().on(bluetoothService).invoke().method("getAddress").withoutArgs()
        address?.let {
            return address as String
        }
        return ""
    }

    fun getCurrentTimeZone(): String {
        return java.util.TimeZone.getDefault().getDisplayName(false, java.util.TimeZone.SHORT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getBatteryStatus(): CharSequence {
        var manager: BatteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager;
        var result = StringBuilder()
        result.append("电池容量").append(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).append("\n")
        result.append("平均容量?").append(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)).append("\n")
        result.append("当前容量?").append(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)).append("\n")
        result.append("当前电量百分比").append(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));///当前电量百分比
        return result
    }

    fun getCountryZipCode(context: Context): String {
        var CountryZipCode = ""
        val locale = context.getResources().getConfiguration().locale
        CountryZipCode = locale.getCountry()

        return CountryZipCode
    }

    fun getSimISO(): String {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simCountryIso
    }

    fun isRooted(): Boolean {
        /**
         * 方式一
         * */
        if (File("/system/bin/su").exists() || File("/system/xbin/su").exists()) {
            return true;
        }
        return false;
    }
    @SuppressLint("WifiManagerLeak")
    fun getWifiInfo():String{
        var mWifi:WifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = mWifi.connectionInfo
        if ( wifiInfo.getMacAddress()== null) {
        }
//      查看已经连接上的WIFI信息，在Android的SDK中为我们提供了一个叫做WifiInfo的对象，这个对象可以通过WifiManager.getConnectionInfo()来获取。WifiInfo中包含了当前连接中的相关信息。
//      getBSSID()  获取BSSID属性
//      getDetailedStateOf()  获取客户端的连通性
//      getHiddenSSID()  获取SSID 是否被隐藏
//      getIpAddress()  获取IP 地址
//      getLinkSpeed()  获取连接的速度
//      getMacAddress()  获取Mac 地址
//      getRssi()  获取802.11n 网络的信号
//      getSSID()  获取SSID
//      getSupplicanState()  获取具体客户端状态的信息
        val sb = StringBuffer()
        sb.append("\n获取BSSID属性（所连接的WIFI设备的MAC地址）：" + wifiInfo.getBSSID())
//      sb.append("getDetailedStateOf()  获取客户端的连通性：");
        sb.append("\n\n获取SSID 是否被隐藏：" + wifiInfo.getHiddenSSID())
        sb.append("\n\n获取IP 地址：" + wifiInfo.getIpAddress())
        sb.append("\n\n获取连接的速度：" + wifiInfo.getLinkSpeed())
        sb.append("\n\n获取Mac 地址（手机本身网卡的MAC地址）：" + wifiInfo.getMacAddress())
        sb.append("\n\n获取802.11n 网络的信号：" + wifiInfo.getRssi())
        sb.append("\n\n获取SSID（所连接的WIFI的网络名称）：" + wifiInfo.getSSID())
        sb.append("\n\n获取具体客户端状态的信息：" + wifiInfo.getSupplicantState())
        return sb.toString()
    }
    fun getUSBVendorID(){
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        manager.deviceList
    }

    fun getMaxCpuFreq(): String {
        var result = ""
        val cmd: ProcessBuilder
        try {
            val args = arrayOf("/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            cmd = ProcessBuilder(*args)
            val process = cmd.start()
            val `in` = process.inputStream
            val re = ByteArray(24)
            while (`in`.read(re) !== -1) {
                result = result + String(re)
            }
            `in`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            result = "N/A"
        }

        return result.trim { it <= ' ' }+"KHz"
    }
    //是否包含GPS
    fun hasGPSDevice(context: Context): Boolean {
        val mgr = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders ?: return false
        return providers.contains(LocationManager.GPS_PROVIDER)
    }
}
