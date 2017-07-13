package online.devliving.passcodeview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Mehedi Hasan Khan (mehedi.mailing@gmail.com).
 */
public class PasscodeView extends ViewGroup{

    EditText mEditText;
    int mDigitCount;

    private int mDigitWidth;
    private int mDigitRadius;
    private int mOuterStrokeWidth;
    private int mInnerStrokeWidth;
    private int mDigitInnerRadius;
    private int mDigitSpacing;
    private int mDigitElevation;

    private int mControlColor;
    private int mHighlightedColor;
    private int mInnerColor;
    private int mInnerBorderColor;

    private OnFocusChangeListener mOnFocusChangeListener;
    private PasscodeEntryListener mPasscodeEntryListener;

    public PasscodeView(Context context) {
        this(context, null);
    }

    public PasscodeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasscodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Get style information
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.PasscodeView);
        mDigitCount = array.getInt(R.styleable.PasscodeView_numDigits, 4);

        // Dimensions
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mDigitRadius = array.getDimensionPixelSize(R.styleable.PasscodeView_digitRadius,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics));
        mOuterStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mInnerStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics);
        mDigitInnerRadius = array.getDimensionPixelSize(R.styleable.PasscodeView_digitInnerRadius,
                mDigitRadius - ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics)));

        if(mDigitInnerRadius > mDigitRadius){
            mDigitInnerRadius = mDigitRadius - ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, metrics));
        }

        mDigitWidth = (mDigitRadius + mOuterStrokeWidth) * 2;

        mDigitSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDigitElevation = array.getDimensionPixelSize(R.styleable.PasscodeView_digitElevation, 0);
        }

        // Get theme to resolve defaults
        Resources.Theme theme = getContext().getTheme();

        mControlColor = Color.DKGRAY;
        // Text colour, default to android:colorControlNormal from theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue controlColor = new TypedValue();
            theme.resolveAttribute(android.R.attr.colorControlNormal, controlColor, true);
            mControlColor = controlColor.resourceId > 0 ? getResources().getColor(controlColor.resourceId) :
                    controlColor.data;
        }
        mControlColor = array.getColor(R.styleable.PasscodeView_controlColor, mControlColor);

        // Accent colour, default to android:colorAccent from theme
        mHighlightedColor = Color.LTGRAY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue accentColor = new TypedValue();
            theme.resolveAttribute(R.attr.colorControlHighlight, accentColor, true);
            mHighlightedColor = accentColor.resourceId > 0 ? getResources().getColor(accentColor.resourceId) :
                    accentColor.data;
        }
        mHighlightedColor = array.getColor(R.styleable.PasscodeView_controlColorActivated, mHighlightedColor);

        //color for the inner circle
        mInnerColor = Color.CYAN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue innerColor = new TypedValue();
            theme.resolveAttribute(android.R.attr.colorPrimary, innerColor, true);
            mInnerColor = innerColor.resourceId > 0 ? getResources().getColor(innerColor.resourceId) :
                    innerColor.data;
        }
        mInnerColor = array.getColor(R.styleable.PasscodeView_digitColorFilled, mInnerColor);

        //color for the inner circle border
        mInnerBorderColor = Color.GREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue innerBorderColor = new TypedValue();
            theme.resolveAttribute(android.R.attr.colorPrimaryDark, innerBorderColor, true);
            mInnerBorderColor = innerBorderColor.resourceId > 0 ? getResources().getColor(innerBorderColor.resourceId) :
                    innerBorderColor.data;
        }
        mInnerBorderColor = array.getColor(R.styleable.PasscodeView_digitColorBorder, mInnerBorderColor);

        // Recycle the typed array
        array.recycle();

        // Add child views
        setupViews();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure children
        for (int i = 0; i < getChildCount(); i ++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Calculate the size of the view
        int width = (mDigitWidth * mDigitCount) + (mDigitSpacing * (mDigitCount - 1));
        setMeasuredDimension(
                width + getPaddingLeft() + getPaddingRight() + (mDigitElevation * 2),
                mDigitWidth + getPaddingTop() + getPaddingBottom() + (mDigitElevation * 2));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Position the child views
        for (int i = 0; i < mDigitCount; i++) {
            View child = getChildAt(i);
            int left = i * mDigitWidth + (i > 0 ? i * mDigitSpacing : 0);
            child.layout(
                    left + getPaddingLeft() + mDigitElevation,
                    getPaddingTop() + (mDigitElevation / 2),
                    left + getPaddingLeft() + mDigitElevation + mDigitWidth,
                    getPaddingTop() + (mDigitElevation / 2) + mDigitWidth);
        }

        // Add the edit text as a 1px wide view to allow it to focus
        getChildAt(mDigitCount).layout(0, 0, 1, getMeasuredHeight());
    }

    private void setupViews(){
        setWillNotDraw(false);
        // Add a digit view for each digit
        for (int i = 0; i < mDigitCount; i++) {
            DigitView digitView = new DigitView(getContext(), i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                digitView.setElevation(mDigitElevation);
            }
            addView(digitView);
        }

        // Add an "invisible" edit text to handle input
        mEditText = new EditText(getContext());
        mEditText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mEditText.setTextColor(getResources().getColor(android.R.color.transparent));
        mEditText.setCursorVisible(false);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mDigitCount)});
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        mEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Update the selected state of the views
                int length = mEditText.getText().length();
                updateChilViewSelectionStates(length, hasFocus);
                // Make sure the cursor is at the end
                mEditText.setSelection(length);

                // Provide focus change events to any listener
                if (mOnFocusChangeListener != null) {
                    mOnFocusChangeListener.onFocusChange(PasscodeView.this, hasFocus);
                }
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                updateChilViewSelectionStates(length, mEditText.hasFocus());

                if (length == mDigitCount && mPasscodeEntryListener != null) {
                    mPasscodeEntryListener.onPasscodeEntered(s.toString());
                }
            }
        });
        addView(mEditText);

        invalidate();
    }

    private void updateChilViewSelectionStates(int length, boolean hasFocus){
        for (int i = 0; i < mDigitCount; i++) {
            getChildAt(i).setSelected(hasFocus && i == length);
        }
    }

    /**
     * Get the {@link Editable} from the EditText
     *
     * @return
     */
    public Editable getText() {
        return mEditText.getText();
    }

    /**
     * Set text to the EditText
     *
     * @param text
     */
    public void setText(CharSequence text) {
        if (text.length() > mDigitCount) {
            text = text.subSequence(0, mDigitCount);
        }
        mEditText.setText(text);
        invalidateChildViews();
    }

    /**
     * Clear passcode input
     */
    public void clearText() {
        mEditText.setText("");
        invalidateChildViews();
    }

    private void invalidateChildViews(){
        for(int i =0; i<mDigitCount; i++)
        {
            getChildAt(i).invalidate();
        }
    }

    public void setPasscodeEntryListener(PasscodeEntryListener mPasscodeEntryListener) {
        this.mPasscodeEntryListener = mPasscodeEntryListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestToShowKeyboard();
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Requests the view to be focused and the keyboard to be popped-up
     */
    public void requestToShowKeyboard(){
        // Make sure this view is focused
        mEditText.requestFocus();

        // Show keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mEditText, 0);
    }

    @Override
    public OnFocusChangeListener getOnFocusChangeListener() {
        return mOnFocusChangeListener;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        mOnFocusChangeListener = l;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.editTextValue = mEditText.getText().toString();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mEditText.setText(savedState.editTextValue);
        mEditText.setSelection(savedState.editTextValue.length());
    }

    static class SavedState extends BaseSavedState {

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        String editTextValue;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            editTextValue = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(editTextValue);
        }
    }

    class DigitView extends View{

        private Paint mOuterPaint, mInnerPaint;
        private int mPosition = 0;

        public DigitView(Context context, int position)
        {
            this(context);
            mPosition = position;
        }

        public DigitView(Context context) {
            this(context, null);
        }

        public DigitView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DigitView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            init();
        }

        void init(){
            setWillNotDraw(false);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
            mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOuterPaint.setAlpha(255);
            mOuterPaint.setDither(true);
            mOuterPaint.setStyle(Paint.Style.STROKE);
            mOuterPaint.setStrokeWidth(mOuterStrokeWidth);
            mOuterPaint.setStrokeCap(Paint.Cap.ROUND);
            mOuterPaint.setStrokeJoin(Paint.Join.ROUND);
            mOuterPaint.setShadowLayer(2, 0, 0, Color.parseColor("#B4999999"));

            mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mInnerPaint.setAlpha(255);
            mInnerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mInnerPaint.setStrokeWidth(mInnerStrokeWidth);
            mInnerPaint.setStrokeCap(Paint.Cap.ROUND);
            mInnerPaint.setStrokeJoin(Paint.Join.ROUND);
            mInnerPaint.setColor(mInnerColor);

            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mDigitWidth, mDigitWidth);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float center = getWidth()/2;

            if(isSelected())
            {
                mOuterPaint.setColor(mHighlightedColor);
            }
            else
            {
                mOuterPaint.setColor(mControlColor);
            }
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawCircle(center, center, mDigitRadius, mOuterPaint);
            if(mEditText.getText().length() > mPosition)
            {
                canvas.drawCircle(center, center, mDigitInnerRadius, mInnerPaint);
            }
        }
    }

    /**
     * Listener that gets notified when the complete passcode has been entered
     */
    public interface PasscodeEntryListener{
        /**
         * Called when all the digits of the passcode has been entered
         * @param passcode - The entered passcode
         */
        void onPasscodeEntered(String passcode);
    }
}