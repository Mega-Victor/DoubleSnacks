
package com.example.DoubleSnacks;
import javax.security.auth.PrivateCredentialPermission;
import com.example.android.BluetoothChat.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    /*
     * 声明图标按钮和数据
     */
    private ImageButton shanglunadd_button;
    private final String shanlunadd_string="0x77 0x01 0x06 0x34 0x01 0x62";
    
    private ImageButton shanglunminus_button;
    private final String shanlunminus_string="0x77 0x01 0x06 0x34 0x02 0x62";
    
    private ImageButton xialunadd_button;
    private final String xialunadd_string="0x77 0x01 0x06 0x35 0x01 0x62";
    
    private ImageButton xialunminus_button;
    private final String xialunminus_string="0x77 0x01 0x06 0x35 0x02 0x62";
    
    private ImageButton gongqiuadd_button;
    private final String gongqiuadd_string="0x77 0x01 0x06 0x31 0x01 0x62";
    
    private ImageButton gongqiuminus_button;
    private final String gongqiuminus_string="0x77 0x01 0x06 0x31 0x02 0x62";
    
    private ImageButton xiangzuo_button;
    private final String xiangzuo_string="0x77 0x01 0x06 0x32 0x01 0x62";
    
    private ImageButton xiangyou_button;
    private final String xiangyou_string="0x77 0x01 0x06 0x32 0x01 0x62";
    
    private ImageButton xiangshang_button;
    private final String xiangshang_string="0x77 0x01 0x06 0x30 0x01 0x62";
    
    private ImageButton xiangxia_button;
    private final String xiangxia_string="0x77 0x01 0x06 0x30 0x02 0x62";
    
    private Button start_button;
    private final String start_string="0x77 0x01 0x06 0x33 0x01 0x62";

    private Button stop_button;
    private final String stop_string="0x77 0x01 0x06 0x33 0x02 0x62";
    
    private Button Ok_button;
    private final String OK_string="0x77 0x01 0x06 0x37 0x01 0x62";
    
    private Button return_button;
    private final String return_string="0x77 0x01 0x06 0x38 0x01 0x62";
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        /*mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);*/

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        /*
         * 上轮转速增加按键事件
         */
        shanglunadd_button=(ImageButton) findViewById(R.id.shanglunadd_button);
        shanglunadd_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(shanlunadd_string);
	                sendMessage(shanlunadd_string);
			}
		});
        
        /*
         * 上轮转速减少按键事件
         */
        shanglunminus_button=(ImageButton) findViewById(R.id.shanglunminus_button);
        shanglunminus_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(shanlunminus_string);
	                sendMessage(shanlunminus_string);
			}
		});
        
        /*
         * 下轮转速增加按键事件
         */
        xialunadd_button=(ImageButton) findViewById(R.id.xialunadd_button);
        xialunadd_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xialunadd_string);
	                sendMessage(xialunadd_string);
			}
		});
        /*
         * 下轮转速减少按键事件
         */
        xialunminus_button=(ImageButton) findViewById(R.id.xialunminus_button);
        xialunminus_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xialunminus_string);
	                sendMessage(xialunminus_string);
			}
		});
        
        /*
         * 供球频率增加按键事件
         */
        gongqiuadd_button=(ImageButton) findViewById(R.id.gongqiuadd_button);
        gongqiuadd_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(gongqiuadd_string);
	                sendMessage(gongqiuadd_string);
			}
		});
        /*
         * 供球频率减少按键事件
         */
        gongqiuminus_button=(ImageButton) findViewById(R.id.gongqiuminus_button);
        gongqiuminus_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(gongqiuminus_string);
	                sendMessage(gongqiuminus_string);
			}
		});
        
        /*
         * 机头旋转向左按键事件
         */
        xiangzuo_button=(ImageButton) findViewById(R.id.xiangzuo_button);
        xiangzuo_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xiangzuo_string);
	                sendMessage(xiangzuo_string);
			}
		});
        /*
         * 机头旋转向右按键事件
         */
       xiangyou_button=(ImageButton) findViewById(R.id.xiangyou_button);
       xiangyou_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xiangyou_string);
	                sendMessage(xiangyou_string);
			}
		});
        
        

       /*
        * 机头调整向上按键事件
        */
      xiangshang_button=(ImageButton) findViewById(R.id.xiangshang_button);
      xiangshang_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xiangshang_string);
	                sendMessage(xiangshang_string);
			}
		});
       /*
        * 机头调整向下按键事件
        */
      xiangxia_button=(ImageButton) findViewById(R.id.xiangxia_button);
      xiangxia_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 TextView view = (TextView) findViewById(R.id.edit_text_out);
	                view.setText(xiangxia_string);
	                sendMessage(xiangxia_string);
			}
		});
	      /*
	       * 启动按键事件
	       */
	     start_button=(Button) findViewById(R.id.start_button);
	     start_button.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 TextView view = (TextView) findViewById(R.id.edit_text_out);
		                view.setText(start_string);
		                sendMessage(start_string);
				}
			});
		     /*
		      * 停止按键事件
		      */
		    stop_button=(Button) findViewById(R.id.stop_button);
		    stop_button.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						 TextView view = (TextView) findViewById(R.id.edit_text_out);
			                view.setText(stop_string);
			                sendMessage(stop_string);
					}
				});
		    /*
		     * 还原按键事件
		     */
		    return_button=(Button) findViewById(R.id.return_button);
		    return_button.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						 TextView view = (TextView) findViewById(R.id.edit_text_out);
			                view.setText(return_string);
			                sendMessage(return_string);
					}
				});
		   
		    /*
		     * OK按键事件
		     */
		    Ok_button=(Button) findViewById(R.id.OK_button);
		    Ok_button.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						 TextView view = (TextView) findViewById(R.id.edit_text_out);
			                view.setText(OK_string);
			                sendMessage(OK_string);
					}
				});
		   
        

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

}