package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ActionEntity::class, EvidenceEntity::class, ReceiptEntity::class, DePinTaskEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ClearanceDatabase : RoomDatabase() {

    abstract fun clearanceDao(): ClearanceDao

    companion object {
        @Volatile
        private var INSTANCE: ClearanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ClearanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClearanceDatabase::class.java,
                    "clearance_solana.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.clearanceDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ClearanceDao) {
            val now = System.currentTimeMillis()

            // Seed DePIN Tasks
            val task1 = DePinTaskEntity(
                id = "HE-5G-8821",
                title = "Helium Mobile 5G Radio Mast Survey",
                network = "Helium Mobile",
                category = "Cellular Coverage",
                description = "Inspect physical antenna orientation, measure 915MHz LoRa & 3.5GHz CBRS RF power, and submit spectrum proof for microcell #4892.",
                rewardAmount = "2.50",
                rewardToken = "SKR",
                deviceOrNodeId = "cbrs_node_mast_8821",
                status = "PROOF_SUBMITTED",
                assignedAt = now - 7200000,
                completedAt = now - 1800000,
                actionId = "act_helium_01",
                receiptId = null,
                requirements = "Upload antenna photo;Log RF spectrum analyzer CSV;Verify GPS altitude > 12m",
                attachedFilesCount = 3
            )
            dao.insertDePinTask(task1)

            val task2 = DePinTaskEntity(
                id = "GRASS-RELAY-412",
                title = "Grass Residential Gateway Uptime",
                network = "Grass Network",
                category = "Bandwidth Relay",
                description = "48-hour bandwidth relay validation with zero packet drops, verified ISP ASN latency <32ms, and 10GB verified egress traffic.",
                rewardAmount = "1.20",
                rewardToken = "SKR",
                deviceOrNodeId = "grass_gw_412_us_west",
                status = "COMPLETED_SETTLED",
                assignedAt = now - 172800000,
                completedAt = now - 86350000,
                actionId = "act_grass_02",
                receiptId = "rec_grass_02",
                requirements = "ISP Gateway diagnostic dump;ASN IP proof;Zero packet drop benchmark",
                attachedFilesCount = 2
            )
            dao.insertDePinTask(task2)

            val task3 = DePinTaskEntity(
                id = "HIVEMAPPER-SURVEY-990",
                title = "Hivemapper 4K Road Photogrammetry",
                network = "Hivemapper",
                category = "Visual Mapping",
                description = "Automotive dashcam 4K photogrammetry survey covering 14.2 km of urban transit corridor with sub-meter RTK positional accuracy.",
                rewardAmount = "3.75",
                rewardToken = "SKR",
                deviceOrNodeId = "dashcam_rtk_cam_990",
                status = "IN_PROGRESS",
                assignedAt = now - 3600000,
                completedAt = null,
                actionId = null,
                receiptId = null,
                requirements = "Capture 4K forward dashcam video;Record GNSS RTK NMEA telemetry;Verify road feature visibility",
                attachedFilesCount = 1
            )
            dao.insertDePinTask(task3)

            val task4 = DePinTaskEntity(
                id = "RENDER-GPU-NODE-77",
                title = "Render Network Octane AI Compute",
                network = "Render Network",
                category = "Decentralized Compute",
                description = "Complete 120-frame raytracing batch job on RTX 4090 cluster with verified VRAM digest and frame hash confirmation.",
                rewardAmount = "5.00",
                rewardToken = "SKR",
                deviceOrNodeId = "render_gpu_cluster_77",
                status = "ASSIGNED",
                assignedAt = now - 900000,
                completedAt = null,
                actionId = null,
                receiptId = null,
                requirements = "Upload compute execution log;Verify SHA-256 of rendered frames;Pass watchdog telemetry check",
                attachedFilesCount = 0
            )
            dao.insertDePinTask(task4)

            // Action 1: Helium Mobile Hotspot PoC Verification (Draft - ready to review)
            val action1 = ActionEntity(
                id = "act_helium_01",
                taskId = "HE-5G-8821",
                title = "Helium Mobile CBRS Coverage Claim",
                description = "Verification of 5G microcell beacon propagation and uptime telemetry for antenna cell #4892",
                category = "DePIN Work",
                recipient = "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
                mint = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84",
                rawAmount = "2500000000", // 2.50 SKR
                decimals = 9,
                network = "devnet",
                status = "DRAFT",
                createdAt = now - 1800000
            )
            dao.insertAction(action1)

            val evidence1 = EvidenceEntity(
                actionId = "act_helium_01",
                photoPath = "simulated://evidence/cbrs_radio_mast_8821.jpg",
                photoSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                videoPath = "simulated://evidence/antenna_panoramic_sweep.mp4",
                videoSha256 = "7a4b89e1f5d23c890124ba56d47e8912c45e78a90123fe4567890123456789ab",
                documentPath = "simulated://evidence/rf_spectrum_analyzer_log.csv",
                documentSha256 = "8f3e21a094b81c7e9014b2d56c87a912e34f67b89012cd34567890123456789c",
                documentName = "rf_spectrum_915mhz_sweep.csv",
                note = "Physical antenna mast mounted at 15m elevation, RF spectrum analyzer confirmed 915MHz LoRa carrier signal and CBRS timing lock.",
                capturedAt = now - 1750000,
                manifestSha256 = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
                deviceAttestationSignature = "MEQCIE/RkP7Fq6mZ9YQzG1D2H3I4J5K6L7M8N9O0P1Q2R3SAiB0C1D2E3F4G5H6I7J8K9L0M1N2O3P4Q5R6S7T8U9V0"
            )
            dao.insertEvidence(evidence1)

            // Action 2: Grass Network Bandwidth Anchor (Settled)
            val action2 = ActionEntity(
                id = "act_grass_02",
                taskId = "GRASS-RELAY-412",
                title = "Grass Residential Bandwidth Relay",
                description = "48-hour bandwidth relay settlement with verified residential IP proof and latency benchmark <32ms",
                category = "Bandwidth Relay",
                recipient = "4Nd1mBQtrMKBCb2u3hGzV8jN4e6V2nK8mP2qR4sT6uV8",
                mint = "SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84",
                rawAmount = "1200000000", // 1.20 SKR
                decimals = 9,
                network = "devnet",
                status = "SETTLED",
                createdAt = now - 86400000
            )
            dao.insertAction(action2)

            val evidence2 = EvidenceEntity(
                actionId = "act_grass_02",
                photoPath = "simulated://evidence/grass_gateway_relay.jpg",
                photoSha256 = "c5d1b32940e791b8a514d4816c141d8a14b532f1469e88cb99583b3842d07ec6",
                videoPath = null,
                videoSha256 = null,
                documentPath = "simulated://evidence/edge_gateway_audit_logs.json",
                documentSha256 = "1d2e3f4a5b6c7d8e9f0123456789abcdef0123456789abcdef0123456789abcd",
                documentName = "edge_gateway_audit_logs.json",
                note = "Edge gateway diagnostic logs hashed; upstream ISP ASN verified clean with zero packet drops over 10GB egress.",
                capturedAt = now - 86350000,
                manifestSha256 = "3a9f02b1c8e76451293adbc5e8f41039478129dcae6189b45012fe849102cba3",
                deviceAttestationSignature = "MEUCIQCv3K6pP9L2mZ8QyG3D4H5I6J7K8L9M0N1O2P3Q4R5SAiEA7G8H9I0J1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z"
            )
            dao.insertEvidence(evidence2)

            val receipt2 = ReceiptEntity(
                id = "rec_grass_02",
                actionId = "act_grass_02",
                taskId = "GRASS-RELAY-412",
                title = "Grass Residential Bandwidth Relay",
                evidenceDigest = "3a9f02b1c8e76451293adbc5e8f41039478129dcae6189b45012fe849102cba3",
                transactionSignature = "4sP9xK8LmZ2NpQ7wY5vT1rC3bF6eD8jH4kN9qR2sT6uV8xY1zA3bC5dE7fG9hJ1k",
                settlementStatus = "FINALIZED",
                observedAt = now - 86300000,
                blockSlot = 318451092L,
                feeLamports = 5000L,
                payer = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                recipient = "4Nd1mBQtrMKBCb2u3hGzV8jN4e6V2nK8mP2qR4sT6uV8",
                amountFormatted = "1.20",
                tokenSymbol = "SKR",
                network = "devnet",
                memo = "CLEARANCE:EVIDENCE_HASH:3a9f02b1c8e76451293adbc5e8f41039478129dcae6189b45012fe849102cba3"
            )
            dao.insertReceipt(receipt2)
        }
    }
}
