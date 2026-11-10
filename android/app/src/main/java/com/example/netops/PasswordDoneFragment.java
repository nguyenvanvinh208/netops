package com.example.netops;

public class PasswordDoneFragment extends BaseFragment {
    @Override protected int layout() { return R.layout.fragment_passdone; }
    @Override protected void init() { tap(R.id.pd_back, () -> nav().popTo("login")); }
}
