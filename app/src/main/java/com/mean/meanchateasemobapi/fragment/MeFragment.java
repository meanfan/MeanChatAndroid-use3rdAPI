package com.mean.meanchateasemobapi.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mean.meanchateasemobapi.R;

public class MeFragment extends Fragment {
    private static final String FRAGMENT_ARG_NICKNAME = "nickname";
    private String nickname;
    private OnFragmentInteractionListener mListener;

    public MeFragment() {
        // Required empty public constructor
    }

    public static MeFragment newInstance(String nickname) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_ARG_NICKNAME, nickname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nickname = getArguments().getString(FRAGMENT_ARG_NICKNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_me, container, false);
        ((TextView)view.findViewById(R.id.tv_user_nickname)).setText(nickname);
        view.findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onLogoutButtonPressed();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onLogoutButtonPressed();
    }
}
