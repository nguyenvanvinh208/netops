package com.example.netops;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/** Lop co so cho moi man hinh: nap layout XML, nut back chung, huy timer khi roi man hinh. */
public abstract class BaseFragment extends Fragment {
    protected View root;
    protected final Handler h = new Handler(Looper.getMainLooper());
    private final ArrayList<Animator> anims = new ArrayList<>();

    protected abstract int layout();
    protected abstract void init();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(layout(), container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View back = root.findViewById(R.id.hdr_back);
        if (back != null) back.setOnClickListener(x -> nav().back());
        init();
    }

    @Override
    public void onDestroyView() {
        h.removeCallbacksAndMessages(null);
        for (Animator a : anims) a.cancel();
        anims.clear();
        super.onDestroyView();
    }

    protected <T extends View> T v(int id) { return root.findViewById(id); }
    protected Nav nav() { return Nav.get(); }
    protected MainActivity act() { return (MainActivity) requireActivity(); }
    protected String arg() { return getArguments() == null ? null : getArguments().getString("arg"); }

    protected void tap(int id, Runnable r) { v(id).setOnClickListener(x -> r.run()); }
    protected void later(long ms, Runnable r) { h.postDelayed(r, ms); }
    protected <A extends Animator> A keep(A a) { anims.add(a); return a; }
}
