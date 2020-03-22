package com.example.srtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private String[] scope = new String[]{VKScope.FRIENDS, VKScope.PHOTOS};
    private Integer nFriends = 0;
    private TextView nFriendsShow;
    String imageUrl;
    ImageView imageView;
    TextView profilePicUrl;
    int imageState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            nFriendsShow.setText(savedInstanceState.getString("textFriends"));
            imageState = savedInstanceState.getInt("avatarImage", R.drawable.ic_launcher_background);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        //String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        //System.out.println(Arrays.asList(fingerprints));
        imageView = (ImageView) findViewById(R.id.avatarImage);

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setContentView(R.layout.activity_main);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("avatarImage", imageState);
        super.onSaveInstanceState(outState);
    }

    public void doLogin(View view) {
        VKSdk.login(this, scope);
    }

    public void doLogout(View view) {
        //VKSdk.login(this, scope);
        VKSdk.logout();
        setContentView(R.layout.start_layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                setContentView(R.layout.activity_main);
                imageView = (ImageView) findViewById(R.id.avatarImage);
                Toast.makeText(getApplicationContext(), "Authorized", Toast.LENGTH_SHORT).show();
                VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "first_name", "last_name"));
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_launcher_foreground).
                        error(R.drawable.ic_launcher_foreground).into(imageView);


                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKList list = (VKList) response.parsedModel;
                        nFriends = list.size();
                        nFriendsShow = (TextView) findViewById(R.id.textFriends);

                        if (nFriends != null) {
                            nFriendsShow.setText(Integer.toString(nFriends) + " friends");
                        }
                    }
                });

                VKParameters params = new VKParameters();
                params.put(VKApiConst.FIELDS, "photo_max_orig");
                VKRequest requestPhoto = new VKRequest("users.get", params);
                requestPhoto.executeWithListener(new VKRequest.VKRequestListener() {

                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        JSONArray resp = null;
                        try {
                            resp = response.json.getJSONArray("response");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONObject user = null;
                        try {
                            user = resp.getJSONObject(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String photoUrl = null;
                        try {
                            photoUrl = user.getString("photo_max_orig");

                            Picasso.get().load(photoUrl).placeholder(R.drawable.ic_launcher_foreground).into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println(photoUrl);

                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);

                    }
                });
            }

            @Override
            public void onError(VKError error) {
                setContentView(R.layout.start_layout);
                Toast.makeText(getApplicationContext(), "Not Authorized", Toast.LENGTH_SHORT).show();

            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
