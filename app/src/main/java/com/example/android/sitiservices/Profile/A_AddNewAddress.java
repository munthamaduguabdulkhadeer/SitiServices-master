package com.example.android.sitiservices.Profile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.android.sitiservices.R;
import com.example.android.sitiservices.Services.D_Address;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class A_AddNewAddress extends AppCompatActivity implements View.OnClickListener, OnTaskCompleted, RadioGroup.OnCheckedChangeListener {

    Button save,gps;
    TextInputLayout textInputLayout1_pincode,textInputLayout2_houseNo,textInputLayout3_road,textInputLayout4_city,textInputLayout5_state,textInputLayout6_name,textInputLayout7_phone;
    TextInputEditText pincode,houseno,roaddno,city,state,phone,name;
    ImageView gpsIcon;
    D_Address d_address=new D_Address();
    Toolbar toolbar;
    RadioGroup radioGroup;
    String AddressType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_address);
        findViewByIds();
        setToolbar();

    }
    public void findViewByIds()
    {
        textInputLayout1_pincode=findViewById(R.id.Add_New_Address_TxtInputLayout1_PinCode);
        textInputLayout2_houseNo=findViewById(R.id.Add_New_Address_TxtInputLayout2_HouseNo);
        textInputLayout3_road=findViewById(R.id.Add_New_Address_TxtInputLayout3_RoadNo);
        textInputLayout4_city=findViewById(R.id.Add_New_Address_TxtInputLayout4_City);
        textInputLayout5_state=findViewById(R.id.Add_New_Address_TxtInputLayout5_State);
        textInputLayout6_name=findViewById(R.id.Add_New_Address_TxtInputLayout6_Name);
        textInputLayout7_phone=findViewById(R.id.Add_New_Address_TxtInputLayout7_phone);

        pincode=findViewById(R.id.Add_New_Address_TxtEditText_PinCode);
        houseno=findViewById(R.id.Add_New_Address_TxtEditText_HouseNumber);
        roaddno=findViewById(R.id.Add_New_Address_TxtEditText_RoadNo);
        city=findViewById(R.id.Add_New_Address_TxtEditText_City);
        state=findViewById(R.id.Add_New_Address_TxtEditText_State);
        phone=findViewById(R.id.Add_New_Address_TxtEditText_Phone);
        name=findViewById(R.id.Add_New_Address_TxtEditText_Name);
        radioGroup=findViewById(R.id.Add_New_Address_RadioBtnGroup);

        gps=findViewById(R.id.Add_New_Address_Use_Current_Location);
        save=findViewById(R.id.Add_New_Address_Btn_Save);
        gpsIcon=findViewById(R.id.Add_New_Address_Gps_Icon);
        gps.setOnClickListener(this);
        save.setOnClickListener(this);
        gpsIcon.setOnClickListener(this);
        toolbar=findViewById(R.id.Toolbar_Add_New_Address);
        radioGroup.setOnCheckedChangeListener(this);
    }
    public void setToolbar()
    {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.Add_New_Address_Btn_Save)
        {
            if (checkPinCode() && checkHouseNo() && checkRoadNo() && checkCity() && checkState() && checkName() && checkPhoneNo() && checkAddressType())
                saveInFirebase();
        }
        else if (v.getId()==R.id.Add_New_Address_Use_Current_Location || v.getId()==R.id.Add_New_Address_Gps_Icon)
        {
            requestPermission();
        }
    }

    private void saveInFirebase() {

        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Addresses")
                .push()
                .setValue(d_address).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Address Stored in FireBase",Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Unable to store in FireBase "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            // Toast.makeText(getApplicationContext(),"Please Turn On Gps",Toast.LENGTH_LONG).show();
            LocationServices.getFusedLocationProviderClient(getApplicationContext()).getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location!=null)
                                new ASyncTaskToFetchAddressUsingGps(A_AddNewAddress.this,A_AddNewAddress.this).execute(location);
                            else
                                Toast.makeText(getApplicationContext(),"Some thing went wrong ",Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1)
        {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                requestPermission();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Please Grant Permission",Toast.LENGTH_LONG).show();
                requestPermission();
            }
        }

    }

    @Override
    public void onTaskCompletedListener(D_Address result) {
        d_address=result;
        if (d_address.error==null)
        {
            pincode.setText(d_address.PinCode);
            houseno.setText(d_address.HouseNo);
            roaddno.setText(d_address.Road_Area_Colony);
            city.setText(d_address.City);
            state.setText(d_address.State);
            phone.setText(d_address.Phone);
            name.setText(d_address.Name);
        }
        else
        {
            Toast.makeText(getApplicationContext()," "+d_address.error,Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkPinCode()
    {
        String pin=pincode.getEditableText().toString();
        if (TextUtils.isEmpty(pin))
        {
            textInputLayout1_pincode.setError("Cannot be empty");
            return false;
        }
        else if (pin.length()==6)
        {
            textInputLayout1_pincode.setError(null);
            d_address.PinCode=pin;
            return true;
        }
        else
        {
            textInputLayout1_pincode.setError("Not a Valid PinCode");
            return false;
        }
    }
    public boolean checkHouseNo()
    {
        String house=houseno.getEditableText().toString();
        if (TextUtils.isEmpty(house))
        {
            textInputLayout2_houseNo.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout2_houseNo.setError(null);
            d_address.HouseNo=house;
            return true;
        }
    }
    public boolean checkRoadNo()
    {
        String roadno=roaddno.getEditableText().toString();
        if (TextUtils.isEmpty(roadno))
        {
            textInputLayout3_road.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout3_road.setError(null);
            d_address.Road_Area_Colony=roadno;
            return true;
        }
    }
    public boolean checkCity()
    {
        String City=city.getEditableText().toString();
        if (TextUtils.isEmpty(City))
        {
            textInputLayout4_city.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout4_city.setError(null);
            d_address.City=City;
            return true;
        }
    }
    public boolean checkState()
    {
        String State=state.getEditableText().toString();
        if (TextUtils.isEmpty(State))
        {
            textInputLayout5_state.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout5_state.setError(null);
            d_address.State=State;
            return true;
        }
    }
    public boolean checkPhoneNo()
    {
        String phoneNo=phone.getEditableText().toString();
        if (TextUtils.isEmpty(phoneNo))
        {
            textInputLayout7_phone.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout7_phone.setError(null);
            d_address.Phone=phoneNo;
            return true;
        }
    }
    public boolean checkName()
    {
        String Name=name.getEditableText().toString();
        if (TextUtils.isEmpty(Name))
        {
            textInputLayout6_name.setError("Cannot be empty");
            return false;
        }
        else
        {
            textInputLayout6_name.setError(null);
            d_address.Name=Name;
            return true;
        }
    }
    public boolean checkAddressType()
    {
        if (AddressType.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please select Address Type",Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            d_address.AddressType=AddressType;
            return true;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId==R.id.Add_New_Address_RadioBtn_Home)
        {
            AddressType="Home";
        }
        else
            AddressType="Office";
    }
}
