package com.example.netops;

import android.os.Bundle;

/** Anh xa route -> Fragment va route -> tab cua BottomNav. */
public final class Router {
    private Router() {}

    public static BaseFragment create(Nav.Route r) {
        BaseFragment f;
        switch (r.id) {
            case "login": f = new LoginFragment(); break;
            case "otp": f = new OtpFragment(); break;
            case "env": f = new EnvFragment(); break;
            case "connecting": f = new ConnectingFragment(); break;
            case "forgot": f = new ForgotFragment(); break;
            case "resetcode": f = new ResetCodeFragment(); break;
            case "newpass": f = new NewPasswordFragment(); break;
            case "passdone": f = new PasswordDoneFragment(); break;
            case "dashboard": f = new DashboardFragment(); break;
            case "server": f = new ServerFragment(); break;
            case "alerts": f = new AlertsFragment(); break;
            case "alertdetail": f = new AlertDetailFragment(); break;
            case "healing": f = new HealingFragment(); break;
            case "containers": f = new ContainersFragment(); break;
            case "container": f = new ContainerDetailFragment(); break;
            case "terminal": f = new TerminalFragment(); break;
            case "images": f = new ImagesFragment(); break;
            case "newdeploy": f = new NewDeploymentFragment(); break;
            case "database": f = new DatabaseFragment(); break;
            case "restore": f = new RestoreFragment(); break;
            case "backupcfg": f = new BackupConfigFragment(); break;
            case "profile": f = new ProfileFragment(); break;
            case "sshkeys": f = new SshKeysFragment(); break;
            case "settings": f = new SettingsFragment(); break;
            default: f = new SplashFragment();
        }
        Bundle b = new Bundle();
        b.putString("arg", r.arg);
        f.setArguments(b);
        return f;
    }

    /** -1 = khong hien thanh dieu huong duoi. */
    public static int tabOf(String id) {
        switch (id) {
            case "dashboard": case "server": return 0;
            case "containers": case "container": case "images": return 1;
            case "database": case "restore": case "backupcfg": return 2;
            case "alerts": return 3;
            case "profile": case "sshkeys": case "settings": return 4;
            default: return -1;
        }
    }
}
