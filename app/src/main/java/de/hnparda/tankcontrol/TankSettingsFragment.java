package de.hnparda.tankcontrol;

import static de.hnparda.tankcontrol.MainActivity.sharedPreferences;
import static de.hnparda.tankcontrol.utils.NodeRequest.GET_DISTANCE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;

import de.hnparda.tankcontrol.utils.NodeRequest;

public class TankSettingsFragment extends Fragment {

    EditText ip;
    EditText port;
    EditText upperLimit;
    EditText lowerLimit;
    MaterialButton getUpperLimitBtn;
    MaterialButton getLowerLimitBtn;
    FloatingActionButton save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.settings_tank, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        ip = view.findViewById(R.id.ip);
        port = view.findViewById(R.id.port);
        upperLimit = view.findViewById(R.id.upperLimit);
        lowerLimit = view.findViewById(R.id.lowerLimit);
        getUpperLimitBtn = view.findViewById(R.id.getUpperLimitBtn);
        getLowerLimitBtn = view.findViewById(R.id.getLowerLimitBtn);
        save = view.findViewById(R.id.save);

        ip.setText(MainActivity.ip);
        port.setText(MainActivity.port);
        upperLimit.setText(String.valueOf(MainActivity.upperLimit));
        lowerLimit.setText(String.valueOf(MainActivity.lowerLimit));
        getUpperLimitBtn.setOnClickListener(v -> getLimit(upperLimit));
        getLowerLimitBtn.setOnClickListener(v -> getLimit(lowerLimit));
        save.setOnClickListener(this::save);
    }

    private void getLimit(EditText limitHolder) {
        try {
            float distance = Float.parseFloat(new NodeRequest().execute(GET_DISTANCE).get());
            limitHolder.setText(String.valueOf(distance));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void save(View view) {
        sharedPreferences.edit()
                .putString("NodeIP", ip.getText().toString())
                .putString("NodePort", port.getText().toString())
                .putFloat("upperLimit", Float.parseFloat(upperLimit.getText().toString()) - 1)
                .putFloat("lowerLimit", Float.parseFloat(lowerLimit.getText().toString()) + 1)
                .apply();
        requireActivity().finish();
    }

}
