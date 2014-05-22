package com.plus.sample.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.plus.sample.R;
import com.plus.sample.adapters.CirclePeopleAdapter;
import com.plus.sample.domain.CirclePerson;
import com.plus.sample.graphic.CircleTransformation;
import com.squareup.picasso.Picasso;

public class MainActivity extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult>,
		View.OnClickListener {

	private static final String TAG = "MainActivity";

	private static final int PROFILE_PIC_SIZE = 350;
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;
	private static final String SAVED_PROGRESS = "sign_in_progress";

	private static final int RC_SIGN_IN = 0;

	private int mSignInProgress;
	private PendingIntent mSignInIntent;

	private GoogleApiClient mGoogleApiClient;

	private SignInButton btnSignIn;
	private Button btnSignOut;
	private Button btnRevoke;
	private LinearLayout llButtons;
	private LinearLayout llProfile;
	private ImageView ivProfile;
	private TextView tvName;
	private TextView tvEmail;
	private ListView lvCircle;
	private CirclePeopleAdapter mCirclesAdapter;
	private List<CirclePerson> mCirclesList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
		btnSignOut = (Button) findViewById(R.id.sign_out_button);
		btnRevoke = (Button) findViewById(R.id.revoke_access_button);
		btnSignIn.setOnClickListener(this);
		btnSignOut.setOnClickListener(this);
		btnRevoke.setOnClickListener(this);

		llButtons = (LinearLayout) findViewById(R.id.ll_buttons);
		llProfile = (LinearLayout) findViewById(R.id.ll_profile);
		ivProfile = (ImageView) findViewById(R.id.profile_imageview);
		tvName = (TextView) findViewById(R.id.name_textview);
		tvEmail = (TextView) findViewById(R.id.email_textview);
		lvCircle = (ListView) findViewById(R.id.circles_list);

		mCirclesAdapter = new CirclePeopleAdapter(this);
		lvCircle.setAdapter(mCirclesAdapter);

		mGoogleApiClient = setGoogleClientBuild();
	}

	private GoogleApiClient setGoogleClientBuild() {
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, null)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVED_PROGRESS, mSignInProgress);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		if (!mGoogleApiClient.isConnecting()) {
			switch (view.getId()) {
			case R.id.sign_in_button:
				resolveSignInError();
				break;

			case R.id.sign_out_button:
				signOutPlus();
				break;

			case R.id.revoke_access_button:
				revokeAccessPlus();
				break;
			}
		}
	}

	private void resolveSignInError() {
		if (mSignInIntent != null) {
			try {
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mSignInProgress != STATE_IN_PROGRESS) {
			mSignInIntent = result.getResolution();
			if (mSignInProgress == STATE_SIGN_IN) {
				resolveSignInError();
			}
		}
		updateUI(false);
	}

	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {

			PersonBuffer personBuffer = peopleData.getPersonBuffer();
			try {
				int count = personBuffer.getCount();
				if (count > 0) {
					mCirclesList = new ArrayList<CirclePerson>();
					for (int i = 0; i < count; i++) {
						CirclePerson cp = new CirclePerson();
						cp.setDisplayName(personBuffer.get(i).getDisplayName());
						cp.setImageUrl(personBuffer.get(i).getImage().getUrl());
						mCirclesList.add(cp);
					}
					loadResults();
				}
			} finally {
				personBuffer.close();
			}
		} else {
			Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		updateUI(true);
		Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		try {
			if (currentPerson != null) {
				String personPhotoUrl = currentPerson.getImage().getUrl();
				tvName.setText(currentPerson.getDisplayName());
				tvEmail.setText(Plus.AccountApi.getAccountName(mGoogleApiClient));
				Picasso.with(this).load(personPhotoUrl.substring(0, personPhotoUrl.length() - 2) + PROFILE_PIC_SIZE).transform(new CircleTransformation())
						.into(ivProfile);
			} else {
				Toast.makeText(getApplicationContext(), "Person information is null", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
		mSignInProgress = STATE_DEFAULT;
	}

	@Override
	public void onConnectionSuspended(int cause) {
		mGoogleApiClient.connect();
		updateUI(false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RC_SIGN_IN:
			if (resultCode == RESULT_OK) {
				mSignInProgress = STATE_SIGN_IN;
			} else {
				mSignInProgress = STATE_DEFAULT;
			}
			if (!mGoogleApiClient.isConnecting()) {
				mGoogleApiClient.connect();
			}
			break;
		}
	}

	private void loadResults() {
		mCirclesAdapter.clear();
		for (CirclePerson item : mCirclesList) {
			mCirclesAdapter.add(item);
		}
		mCirclesAdapter.notifyDataSetChanged();
	}

	private void signOutPlus() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();
			updateUI(false);
		}
	}

	private void revokeAccessPlus() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
			mGoogleApiClient = setGoogleClientBuild();
			mGoogleApiClient.connect();
			updateUI(false);
		}
	}

	private void updateUI(boolean isSignedIn) {
		if (isSignedIn) {
			btnSignIn.setEnabled(false);
			btnSignOut.setEnabled(true);
			btnRevoke.setEnabled(true);

			btnSignIn.setVisibility(View.GONE);
			llButtons.setVisibility(View.VISIBLE);
			llProfile.setVisibility(View.VISIBLE);
			lvCircle.setVisibility(View.VISIBLE);
		} else {
			btnSignIn.setEnabled(true);
			btnSignOut.setEnabled(false);
			btnRevoke.setEnabled(false);

			btnSignIn.setVisibility(View.VISIBLE);
			llButtons.setVisibility(View.GONE);
			llProfile.setVisibility(View.GONE);
			lvCircle.setVisibility(View.GONE);
		}
	}

}
