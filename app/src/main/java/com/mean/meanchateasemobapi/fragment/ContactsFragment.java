package com.mean.meanchateasemobapi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.widget.EaseContactList;
import com.mean.meanchateasemobapi.R;


public class ContactsFragment extends Fragment {
    private EaseContactList contactList;

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
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onContactFragmentStart(contactList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setContactsFragmentInteractionListener(OnContactsFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public interface OnContactsFragmentInteractionListener {
        void onContactFragmentStart(EaseContactList chatList);
    }
}
