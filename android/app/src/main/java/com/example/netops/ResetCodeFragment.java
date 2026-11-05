package com.example.netops;

public class ResetCodeFragment extends OtpFragment {
    @Override protected String title() { return "Check your email"; }
    @Override protected String body() { return "We sent a 6-digit reset code to ma***@netops.io"; }
    @Override protected String timerLabel() { return "Code expires in"; }
    @Override protected String btnText() { return "Verify code"; }
    @Override protected String linkText() { return "Use a different email"; }
    @Override protected int boxStroke() { return UI.alpha(C.BLUE, 0.5f); }
    @Override protected void onVerify() { nav().go("newpass"); }
    @Override protected void onLink() { nav().back(); }
}
