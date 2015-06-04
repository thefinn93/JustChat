package ninja.justchat;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

public class ChatActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    static public KeyManager[] keymanagers;

    static public String name = "";
    static public ArrayList<Channel> channels = new ArrayList<>();
    static public Channel currentChannel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Setup the key store and try to load the saved
        boolean certloaded = false;
        try {
            Log.d("LoadKey", "Preparing to load old key");
            KeyStore keystore = KeyStore.getInstance("BKS");
            FileInputStream fis = ChatActivity.this.openFileInput("user.ks");
            keystore.load(fis, "PcSo9XngI6pvbwRM8aCs7ZE4RHwGxnau".toCharArray());
            char[] keyStorePassword = "PcSo9XngI6pvbwRM8aCs7ZE4RHwGxnau".toCharArray();
            if (keystore.containsAlias("JustChatUser")) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");

                kmf.init(keystore, keyStorePassword);

                Log.v("LoadKey", "Keystore open, contains JustChatUser key");
                keymanagers = kmf.getKeyManagers();
                certloaded = true;
            } else {
                Log.d("LoadKey", "keystore loaded, but does not have an key named JustChatUser");
            }
        } catch (KeyStoreException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
            e.printStackTrace();
        }  catch (FileNotFoundException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
        } catch (CertificateException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            Log.d("LoadKey", "Failed to load key: " + e.toString());
            e.printStackTrace();
        }
        if(!certloaded) {
            Log.d("LoadKey", "Key not loaded, launching NameDialog activity...");
            new NameDialog(ChatActivity.this).onClick(new View(this));
        }
    }

    public ChatActivity() {
        channels.add(new Channel("Chennel"));
        currentChannel=channels.get(0);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ChatFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
//                mTitle = getString(R.string.title_section1);
                break;
            case 2:
//                mTitle = getString(R.string.title_section2);
                break;
            case 3:
//                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshChatLog();
            return true;
        }
        if (id == R.id.action_join) {
            new ChannelSelect(ChatActivity.this).onClick(new View(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshChatLog() {
        try {
            JSONObject refreshChatLogCall = new JSONObject();
            refreshChatLogCall.put("action", "refresh");
            SecureConnectionCallback callback = new SecureConnectionCallback();
            new SecureConnection(callback, ChatActivity.keymanagers).execute(refreshChatLogCall);
        } catch (JSONException e) {
            Log.d("RefreshFailure", "Failed to refresh chat log");
            e.printStackTrace();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ChatFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //private ArrayList<String> chatLogList = new ArrayList<>();
        private ArrayAdapter<String> chatLogAdapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ChatFragment newInstance(int sectionNumber) {
            ChatFragment fragment = new ChatFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public ChatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_chat, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            final EditText editTextEntryBox = (EditText) getActivity().findViewById(R.id.entryBox);
            editTextEntryBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    ListView chatLogList = (ListView) getActivity().findViewById(R.id.chatLogList);
                    chatLogAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, currentChannel.chatLog);
                    chatLogList.setAdapter(chatLogAdapter);
                    chatLogList.setSelection(chatLogAdapter.getCount() - 1);
                    // Send message to server
                    JSONObject dataToSend = new JSONObject();
                    try {
                        dataToSend.put("action", "sendmsg");
                        dataToSend.put("channel", "Chennel");
                        dataToSend.put("message", editTextEntryBox.getText().toString());
                    } catch(JSONException e) {
                        Log.e("JSONEncoding", e.toString());
                        e.printStackTrace();
                    }

                    // Network code
                    SecureConnectionCallback callback = new SecureConnectionCallback(getActivity());
                    new SecureConnection(callback, keymanagers).execute(dataToSend);


                    // Add it to the list
                    //currentChannel.chatLog.add(name + ">" + editTextEntryBox.getText().toString());
                    //chatLog.scrollTo(0, chatLog.getLayout().getLineTop(chatLog.getLineCount()) - chatLog.getHeight());
                    v.setText("");
                    return true;
                }
            });
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ChatActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void onAPIResponse(JSONObject result) {
        Log.d("onAPIResponse", result.toString());
    }

}
