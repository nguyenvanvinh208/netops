package com.example.netops.net;

import java.util.List;

/** Các lớp dữ liệu khớp JSON của backend. */
public final class Models {
    private Models() {}

    public static class LoginRequest {
        public String email, password;
        public LoginRequest(String email, String password) { this.email = email; this.password = password; }
    }
    public static class LoginResponse { public String message, token; }

    public static class ServerDto {
        public int id;
        public String name, ipAddress, status;
        public double cpu, ram, disk;   // phần trăm, đo thật bằng SSH
        public long uptime;             // giây
    }
    public static class ContainerDto {
        public int id, serverId;
        public String name, image, status;
        public double cpu;              // % CPU của container
    }
    public static class AttentionDto {
        public String title, sub, severity; // severity: critical | warning
        public int serverId;
    }
    public static class Overview {
        public int totalServers, totalContainers, alerts;
        public List<ServerDto> servers;
        public List<ContainerDto> containers;
        public List<AttentionDto> attention;
        public String updatedAt;
    }
}
