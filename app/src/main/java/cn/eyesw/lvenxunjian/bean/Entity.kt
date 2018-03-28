package cn.eyesw.lvenxunjian.bean

/**
 * 实体类
 */

/* 抢维修 */
data class RepairManagerEntity(var did: String, var dangerType: String, var dangerLevel: String,
                               var staffName: String, var dnote: String, var address: String,
                               var ctime: String, var managerName: String, var latitude: String,
                               var longitude: String)

/* 数据采集 */
data class DataCollectionEntity(var resId: Int, var title: String)

/* 数据信息 */
data class DataInfoEntity(var dataId: String, var createTime: String, var address: String)

/* 轨迹 */
data class PositionJsonBean(var staff_id: String, var latitude: Double, var longitude: Double,
                            var status: Int, var length: Double, var createtime: String)
