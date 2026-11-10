package com.example.netops;

/** Du lieu mau (khong co backend) - lay nguyen tu ban Compose. */
public final class Data {
    private Data() {}

    // ---------------------------------------------------------------- moi truong
    public static final class Env {
        public final String name, tag, sub, servers, ping, icon;
        public final int tagColor, iconColor;
        Env(String name, String tag, int tagColor, String sub, String servers, String ping, String icon, int iconColor) {
            this.name = name; this.tag = tag; this.tagColor = tagColor; this.sub = sub; this.servers = servers;
            this.ping = ping; this.icon = icon; this.iconColor = iconColor;
        }
    }
    public static final Env[] ENVS = {
        new Env("Production", "PROD", C.RED, "Azure \u00b7 West Europe", "2 / 2", "42 ms", "cloud", C.CYAN),
        new Env("Staging", "STAGE", C.AMBER, "AWS \u00b7 ap-southeast-1", "5 / 5", "58 ms", "cloud", C.VIOLET),
        new Env("Development", "DEV", C.GREEN, "Local \u00b7 192.168.1.0/24", "2 / 2", "3 ms", "drive", C.GREEN)
    };
    public static final String[] ENV_SERVERS = {"2 servers", "5 servers", "2 servers"};

    // ---------------------------------------------------------------- dashboard
    public static final class Srv {
        public final String sub; public final int cpu, ram, spi; public final float[] cpuS, ramS;
        Srv(String sub, int cpu, int ram, int spi, float[] cpuS, float[] ramS) {
            this.sub = sub.replace(" \u00b7 ", " \u00b7 "); this.cpu = cpu; this.ram = ram; this.spi = spi; this.cpuS = cpuS; this.ramS = ramS;
        }
    }
    public static final Srv[] SRV = {
        new Srv("2 servers online", 68, 42, 99, new float[]{52f, 55f, 54f, 58f, 61f, 60f, 64f, 66f, 65f, 68f, 70f, 67f, 68f}, new float[]{41f, 41f, 42f, 41f, 42f, 42f, 41f, 42f, 42f, 43f, 42f, 42f, 42f}),
        new Srv("Server 1 · online", 57, 38, 99, new float[]{48f, 50f, 49f, 52f, 55f, 53f, 56f, 58f, 55f, 57f, 59f, 56f, 57f}, new float[]{36f, 37f, 37f, 38f, 37f, 38f, 38f, 39f, 38f, 38f, 39f, 38f, 38f}),
        new Srv("Server 2 · online", 79, 46, 98, new float[]{60f, 66f, 62f, 72f, 70f, 78f, 74f, 83f, 77f, 81f, 76f, 80f, 79f}, new float[]{40f, 41f, 43f, 42f, 44f, 43f, 45f, 44f, 46f, 45f, 47f, 46f, 46f})
    };

    /** Disk % theo server (index 1, 2) va CPU avg/peak/min theo [server-1][range 1H,24H,7D,30D]. */
    public static final int[] SRV_DISK = {0, 61, 71};
    public static final String[][][] SRV_STATS = {
        {{"56%", "68%", "38%"}, {"56%", "69%", "38%"}, {"55%", "71%", "40%"}, {"55%", "68%", "40%"}},
        {{"70%", "88%", "57%"}, {"72%", "91%", "58%"}, {"69%", "86%", "52%"}, {"71%", "89%", "53%"}}};

    public static final class Perf {
        public final float[] cpu, ram; public final float cpuTop, ramTop; public final String[] labels; public final String avg, peak, min;
        Perf(float[] cpu, float cpuTop, float[] ram, float ramTop, String[] labels, String avg, String peak, String min) {
            this.cpu = cpu; this.cpuTop = cpuTop; this.ram = ram; this.ramTop = ramTop; this.labels = labels;
            this.avg = avg; this.peak = peak; this.min = min;
        }
    }
    public static final Perf[] PERF = {
        new Perf(new float[]{20.9f, 11.9f, 0f, 9.2f, 10.1f, 2.2f, 16.7f, 20.9f, 29.1f, 27.3f, 29.6f, 6.9f, 2f}, 49.7f, new float[]{8.8f, 9.7f, 3.5f, 3.9f, 7.1f, 6.1f, 4.9f, 2.3f, 2.9f, 3f, 0f, 4.5f, 3.3f}, 102.4f, new String[]{"-60m", "-45m", "-30m", "-15m", "Now"}, "67%", "75%", "59%"),
        new Perf(new float[]{81.9f, 54.6f, 47.3f, 45.5f, 40f, 3.6f, 40f, 3.6f, 0f, 21.8f}, 40.4f, new float[]{18.2f, 12.7f, 10.9f, 1.8f, 14.6f, 5.5f, 9.1f, 0f, 12.7f, 10.9f}, 95f, new String[]{"00:00", "06:00", "12:00", "18:00", "Now"}, "54%", "92%", "18%"),
        new Perf(new float[]{75.4f, 11.9f, 12.1f, 60.7f, 79.1f, 4f, 3.4f, 55.3f, 63.6f, 14.9f, 5.3f, 66.5f, 62.4f, 21.4f, 10.6f, 58.6f, 78.1f, 8.4f, 0f, 62.8f, 72.9f, 14.1f, 6.5f, 49.5f, 72.3f, 14.7f, 7.7f, 55.7f, 76.8f}, 49.9f, new float[]{11.2f, 9.9f, 9.1f, 12.3f, 6.1f, 7.5f, 6.4f, 4.3f, 5.4f, 2.3f, 3.8f, 1.8f, 3.6f, 0f, 2.8f, 4.7f, 2.1f, 5.4f, 1.2f, 3.6f, 5.8f, 4.4f, 8.3f, 6.7f, 5.8f, 11.3f, 8.4f, 12f, 13.5f}, 99.7f, new String[]{"Oct 8", "Oct 10", "Oct 12", "Oct 14", "Now"}, "53%", "75%", "31%"),
        new Perf(new float[]{73f, 57.6f, 48.1f, 57.3f, 56.4f, 55.2f, 61.2f, 59.4f, 65.1f, 70.2f, 71.4f, 65.5f, 73.2f, 74.7f, 96f, 72.6f, 79.6f, 86.8f, 76.2f, 60.5f, 71.2f, 0f, 36f, 55.7f, 57.9f, 47.6f, 65.8f, 64.7f, 60.5f, 82.5f, 88.8f}, 25.8f, new float[]{21.5f, 19.8f, 17f, 17.1f, 15.1f, 17.5f, 14.7f, 18f, 14.3f, 18.2f, 14.3f, 10.7f, 13.4f, 14.1f, 12.9f, 10.4f, 11.6f, 7.2f, 12.2f, 5.3f, 7.5f, 6.3f, 9.1f, 5.9f, 4.1f, 1.5f, 4.5f, 6.5f, 3f, 3.4f, 0f}, 96.3f, new String[]{"Sep 15", "Sep 22", "Sep 29", "Oct 6", "Now"}, "53%", "88%", "35%")
    };

    // ---------------------------------------------------------------- containers
    public static final class CInfo {
        public final String id, name, sub, icon, vcpu, ramGb, up, image, digest, policy, sessions;
        public final int server, tile, cpu, ram;
        public final String[] ports;
        CInfo(String id, String name, String sub, int server, int tile, String icon, int cpu, int ram, String vcpu, String ramGb,
              String up, String[] ports, String image, String digest, String policy, String sessions) {
            this.id = id; this.name = name; this.sub = sub; this.server = server; this.tile = tile; this.icon = icon;
            this.cpu = cpu; this.ram = ram; this.vcpu = vcpu; this.ramGb = ramGb; this.up = up; this.ports = ports;
            this.image = image; this.digest = digest; this.policy = policy; this.sessions = sessions;
        }
    }
    public static final CInfo[] CONTAINERS = {
        new CInfo("web-api", "C# Web API", "web-api \u00b7 Server 1 \u00b7 :80 \u00b7 .NET 8.0", 1, C.VIOLET, "box", 38, 62, "0.38 / 1.0 vCPU", "1.24 / 2.0 GB",
            "6d 14h 22m", new String[]{"80:80", "443:443"}, "web-api:8.0", "sha256:9f2c\u2026a41e", "unless-stopped", "web-api \u00b7 1,284 active sessions"),
        new CInfo("postgres", "PostgreSQL", "postgres \u00b7 Server 2 \u00b7 :5432 \u00b7 v16.3", 2, C.BLUE, "db", 22, 47, "0.22 / 1.0 vCPU", "0.94 / 2.0 GB",
            "12d 3h 05m", new String[]{"5432:5432"}, "postgres:16.3", "sha256:3b7d\u2026e90c", "always", "postgres \u00b7 96 active connections"),
        new CInfo("sql-server", "SQL Server", "sql-server \u00b7 Server 2 \u00b7 :1433 \u00b7 2022", 2, C.AMBER, "db", 71, 88, "0.71 / 1.0 vCPU", "3.52 / 4.0 GB",
            "2d 9h 41m", new String[]{"1433:1433"}, "mssql:2022", "sha256:c41a\u202607fb", "on-failure", "sql-server \u00b7 41 active connections")
    };
    public static CInfo cinfo(String id) {
        for (CInfo c : CONTAINERS) if (c.id.equals(id)) return c;
        return CONTAINERS[0];
    }

    public static final class Img {
        public final String id, name, digest, tag, icon, size, pushed; public final int tile;
        Img(String id, String name, String digest, String tag, int tile, String icon, String size, String pushed) {
            this.id = id; this.name = name; this.digest = digest; this.tag = tag; this.tile = tile; this.icon = icon; this.size = size; this.pushed = pushed;
        }
    }
    public static final Img[] IMAGES = {
        new Img("web-api", "C# Web API", "sha256:9f2c\u2026a41e", ".NET 8.0", C.VIOLET, "box", "212 MB", "2d ago"),
        new Img("postgres", "PostgreSQL", "sha256:3b7d\u2026e90c", "16.3", C.BLUE, "db", "148 MB", "5d ago"),
        new Img("sql-server", "SQL Server", "sha256:c41a\u202607fb", "2022", C.AMBER, "db", "1.6 GB", "1w ago")
    };

    // ---------------------------------------------------------------- database
    public static final class Snap {
        public final String title, src, size; public final boolean manual;
        Snap(String title, String src, String size, boolean manual) { this.title = title; this.src = src; this.size = size; this.manual = manual; }
    }
    public static final class Db {
        public final String name, engine, health, storageTxt, connTxt, rwTxt, lastBackup, replicas, total, compressSave, time;
        public final int healthColor, storage, conn, rw, points; public final Snap[] snaps;
        Db(String name, String engine, String health, int healthColor, int storage, String storageTxt, int conn, String connTxt, int rw, String rwTxt,
           String lastBackup, String replicas, int points, String total, Snap[] snaps, String compressSave, String time) {
            this.name = name; this.engine = engine; this.health = health; this.healthColor = healthColor; this.storage = storage; this.storageTxt = storageTxt;
            this.conn = conn; this.connTxt = connTxt; this.rw = rw; this.rwTxt = rwTxt; this.lastBackup = lastBackup; this.replicas = replicas;
            this.points = points; this.total = total; this.snaps = snaps; this.compressSave = compressSave; this.time = time;
        }
    }
    public static final Db[] DBS = {
        new Db("PostgreSQL", "PostgreSQL 16.3", "HEALTHY", C.GREEN, 72, "36 / 50 GB", 48, "96 / 200", 86, "1.2k IOPS",
            "02:00 UTC \u00b7 846 MB", "2 healthy \u00b7 lag 0.4s", 24, "14.2 GB",
            new Snap[]{
                new Snap("Today \u00b7 02:00 UTC", "pg_dump \u00b7 ZSTD \u00b7 Azure Blob", "846 MB", false),
                new Snap("Yesterday \u00b7 02:00 UTC", "pg_dump \u00b7 ZSTD \u00b7 Azure Blob", "838 MB", false),
                new Snap("Mon 12 Oct \u00b7 14:32", "pg_dump \u00b7 ZSTD \u00b7 Local disk", "512 MB", true),
                new Snap("Sun 11 Oct \u00b7 02:00 UTC", "pg_dump \u00b7 ZSTD \u00b7 Azure Blob", "829 MB", false)},
            "ZSTD \u00b7 saves ~62% storage", "02:00 UTC"),
        new Db("SQL Server", "SQL Server 2022", "SLOW", C.AMBER, 58, "29 / 50 GB", 64, "128 / 200", 91, "2.4k IOPS",
            "03:00 UTC \u00b7 1.4 GB", "1 healthy \u00b7 lag 1.8s", 18, "21.6 GB",
            new Snap[]{
                new Snap("Today \u00b7 03:00 UTC", ".bak \u00b7 ZSTD \u00b7 Azure Blob", "1.4 GB", false),
                new Snap("Yesterday \u00b7 03:00 UTC", ".bak \u00b7 ZSTD \u00b7 Azure Blob", "1.38 GB", false),
                new Snap("Mon 12 Oct \u00b7 14:32", ".bak \u00b7 ZSTD \u00b7 Local disk", "880 MB", true),
                new Snap("Sun 11 Oct \u00b7 03:00 UTC", ".bak \u00b7 ZSTD \u00b7 Azure Blob", "1.36 GB", false)},
            "ZSTD \u00b7 saves ~54% storage", "03:00 UTC")
    };
}
