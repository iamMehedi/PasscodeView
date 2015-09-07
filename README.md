# PasscodeView
An android widget to input passcode.

##Usage
`PasscodeView` is a `ViewGroup` subclass. So it can easily be added in any xml layout files.

```xml
<com.mhk.android.passcodeview.PasscodeView
        android:id="@+id/passcode_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        passcodeView:numDigits="5"
        android:layout_marginTop="@dimen/activity_vertical_margin"
        />
```

###Methods
- `requestToShowKeyboard()` - Request the PasscodeView to be focused programmatically
- `setText(CharSequence text)` - Set Passcode programmatically
- `clearText()` - Clear Passcode
- `getText()` - get entered Passcode
- `setPasscodeEntryListener(PasscodeEntryListener mPasscodeEntryListener)` - Set a listener to get notified when the Passcode has been entered

###Listener:`PasscodeEntryListener`
- `onPasscodeEntered(String passcode)` - Called when all the digits of the passcode has been entered

```java
passcodeView.setPasscodeEntryListener(new PasscodeView.PasscodeEntryListener() {
            @Override
            public void onPasscodeEntered(String passcode) {
                Toast.makeText(SampleActivity.this, "Passcode entered: " + passcode, Toast.LENGTH_SHORT).show();
            }
        });
```

###XML Attributes
- `numDigits` - Number of passcode digits
- `digitElevation` - Elevation of each digit, only applicable for OS version >= Lollipop
- `digitRadius` - radius for digit circle `16dip` by default
- `controlColor` - color of the outer circle in normal state, by default `android:colorControlNormal`
- `controlColorActivated` - color of outer circle when focused, by default `android:colorControlHighlighted`
- `digitColorFilled` - fill color of the inner circle, by default `android:colorPrimary`
- `digitColorBorder` - border color of the inner circle, by default `android:colorPrimaryDark`

