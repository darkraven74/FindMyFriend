package ru.ifmo.findmyfriend.about;


import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.ifmo.findmyfriend.R;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.about_fragment, container, false);
        TextView mailtoAvgarder = (TextView) rootView.findViewById(R.id.mailto_avgarder);
        TextView mailtoDarkraven8 = (TextView) rootView.findViewById(R.id.mailto_darkraven8);
        TextView version = (TextView) rootView.findViewById(R.id.version);

        mailtoAvgarder.setMovementMethod(LinkMovementMethod.getInstance());
        mailtoDarkraven8.setMovementMethod(LinkMovementMethod.getInstance());

        String versionName = null;
        try {
            versionName = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version.setText(version.getText() + " " + versionName);
        return rootView;
    }
}