package de.hnparda.tankcontrol;

import static de.hnparda.tankcontrol.MainActivity.sharedPreferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NodeSettingsFragment extends Fragment {

    EditText ip;
    EditText port;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_node, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ip = view.findViewById(R.id.ip);
        port = view.findViewById(R.id.port);
       // ip.setText(sharedPreferences.getString("NodeIP", ""));
        //port.setText(sharedPreferences.getString("NodePort", "80"));
    }


    public void save(View view) {
        sharedPreferences.edit().putString("NodeIP", ip.getText().toString()).putString("NodePort", port.getText().toString()).apply();
        getActivity().finish();
    }

}
