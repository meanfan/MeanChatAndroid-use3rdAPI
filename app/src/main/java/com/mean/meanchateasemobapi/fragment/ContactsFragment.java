package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.widget.EaseContactList;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.mean.meanchateasemobapi.R;


public class ContactsFragment extends Fragment {
    private EaseContactList contactList;
    private EaseTitleBar titleBar;
    private TextView tv_message;
    private ImageView iv_message_dot;

    private OnContactsFragmentInteractionListener mListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactList = view.findViewById(R.id.contact_list);
        tv_message = view.findViewById(R.id.tv_message);
        iv_message_dot = view.findViewById(R.id.iv_message_dot);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onContactsFragmentStart(contactList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void clearMessageView(){
        tv_message.setText(R.string.message_default);
        iv_message_dot.setVisibility(View.INVISIBLE);
    }

    public void setMessageView(String message){
        tv_message.setText(message);
        iv_message_dot.setVisibility(View.VISIBLE);
    }

    public void setContactsFragmentInteractionListener(OnContactsFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public void refresh(){
        contactList.refresh();
    }

    public interface OnContactsFragmentInteractionListener {
        void onContactsFragmentStart(EaseContactList chatList);
    }
}
