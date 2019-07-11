package com.mrtrying.widget.expandabletext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Description :
 * PackageName : com.mrtrying.widget
 * Created by mrtrying on 2019/4/17 17:21.
 * e_mail : ztanzeyu@gmail.com
 */
@SuppressLint("AppCompatCustomView")
public class ExpandableTextView extends TextView {
    public static final String TAG = ExpandableTextView.class.getSimpleName();

    public static final String ELLIPSIS_STRING = new String(new char[]{'\u2026'});
    private static final int DEFAULT_MAX_LINE = 3;
    private static final String DEFAULT_OPEN_SUFFIX = " 展开";
    private static final String DEFAULT_CLOSE_SUFFIX = " 收起";
    volatile boolean animating = false;
    boolean isClosed = false;
    private int mMaxLines = DEFAULT_MAX_LINE;
    /** TextView可展示宽度，包含paddingLeft和paddingRight */
    private int initWidth = 0;
    /** 原始文本 */
    private CharSequence originalText;

    private SpannableStringBuilder mOpenSpannableStr, mCloseSpannableStr;

    private boolean hasAnimation = false;
    private Animation mOpenAnim, mCloseAnim;
    private int mOpenHeight, mCLoseHeight;
    private boolean mExpandable;
    private boolean mCloseInNewLine;
    @Nullable
    private SpannableString mOpenSuffixSpan, mCloseSuffixSpan;
    private String mOpenSuffixStr = DEFAULT_OPEN_SUFFIX;
    private String mCloseSuffixStr = DEFAULT_CLOSE_SUFFIX;
    private int mOpenSuffixColor, mCloseSuffixColor;

    private View.OnClickListener mOnClickListener;

    private CharSequenceToSpannableHandler mCharSequenceToSpannableHandler;

    public ExpandableTextView(Context context) {
        super(context);
        initialize();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    /** 初始化 */
    private void initialize() {
        mOpenSuffixColor = mCloseSuffixColor = Color.parseColor("#F23030");
        setMovementMethod(LinkMovementMethod.getInstance());
        setIncludeFontPadding(false);
        updateOpenSuffixSpan();
        updateCloseSuffixSpan();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setOriginalText(CharSequence originalText) {
        this.originalText = originalText;
        mExpandable = false;
        mCloseSpannableStr = new SpannableStringBuilder();
        final int maxLines = mMaxLines;
        SpannableStringBuilder tempText = charSequenceToSpannable(originalText);
        mOpenSpannableStr = charSequenceToSpannable(originalText);

        if (maxLines != -1) {
            Layout layout = createStaticLayout(tempText);
            mExpandable = layout.getLineCount() > maxLines;
            if (mExpandable) {
                //拼接展开内容
                if (mCloseInNewLine) {
                    mOpenSpannableStr.append("\n");
                }
                if (mCloseSuffixSpan != null) {
                    mOpenSpannableStr.append(mCloseSuffixSpan);
                }
                //计算原文截取位置
                int endPos = layout.getLineEnd(maxLines - 1);
                if (originalText.length() <= endPos) {
                    mCloseSpannableStr = charSequenceToSpannable(originalText);
                } else {
                    mCloseSpannableStr = charSequenceToSpannable(originalText.subSequence(0, endPos));
                }
                SpannableStringBuilder tempText2 = charSequenceToSpannable(mCloseSpannableStr).append(ELLIPSIS_STRING);
                if (mOpenSuffixSpan != null) {
                    tempText2.append(mOpenSuffixSpan);
                }
                //循环判断，收起内容添加展开后缀后的内容
                Layout tempLayout = createStaticLayout(tempText2);
                while (tempLayout.getLineCount() > maxLines) {
                    int lastSpace = mCloseSpannableStr.length() - 1;
                    if (lastSpace == -1) {
                        break;
                    }
                    if (originalText.length() <= lastSpace) {
                        mCloseSpannableStr = charSequenceToSpannable(originalText);
                    } else {
                        mCloseSpannableStr = charSequenceToSpannable(originalText.subSequence(0, lastSpace));
                    }
                    tempText2 = charSequenceToSpannable(mCloseSpannableStr).append(ELLIPSIS_STRING);
                    if (mOpenSuffixSpan != null) {
                        tempText2.append(mOpenSuffixSpan);
                    }
                    tempLayout = createStaticLayout(tempText2);

                }
                int lastSpace = mCloseSpannableStr.length() - mOpenSuffixSpan.length();
                if(lastSpace >= 0 && originalText.length() > lastSpace){
                    mCloseSpannableStr = charSequenceToSpannable(originalText.subSequence(0, lastSpace));
                }
                //计算收起的文本高度
                mCLoseHeight = tempLayout.getHeight() + getPaddingTop() + getPaddingBottom();

                mCloseSpannableStr.append(ELLIPSIS_STRING);
                if (mOpenSuffixSpan != null) {
                    mCloseSpannableStr.append(mOpenSuffixSpan);
                }
            }
        }
        isClosed = mExpandable;
        if (mExpandable) {
            setText(mCloseSpannableStr);
            //设置监听
            super.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    switchOpenClose();
//                    if (mOnClickListener != null) {
//                        mOnClickListener.onClick(v);
//                    }
                }
            });
        } else {
            setText(mOpenSpannableStr);
        }
    }

    private void switchOpenClose() {
        if (mExpandable) {
            isClosed = !isClosed;
            if (isClosed) {
                close();
            } else {
                open();
            }
        }
    }

    /**
     * 设置是否有动画
     *
     * @param hasAnimation
     */
    public void setHasAnimation(boolean hasAnimation) {
        this.hasAnimation = hasAnimation;
    }

    /** 展开 */
    private void open() {
        if (hasAnimation) {
            Layout layout = createStaticLayout(mOpenSpannableStr);
            mOpenHeight = layout.getHeight() + getPaddingTop() + getPaddingBottom();
            executeOpenAnim();
        } else {
            ExpandableTextView.super.setMaxLines(Integer.MAX_VALUE);
            setText(mOpenSpannableStr);
            if (mOpenCloseCallback != null){
                mOpenCloseCallback.onOpen();
            }
        }
    }

    /** 收起 */
    private void close() {
        if (hasAnimation) {
            executeCloseAnim();
        } else {
            ExpandableTextView.super.setMaxLines(mMaxLines);
            setText(mCloseSpannableStr);
            if (mOpenCloseCallback != null){
                mOpenCloseCallback.onClose();
            }
        }
    }

    /** 执行展开动画 */
    private void executeOpenAnim() {
        //创建展开动画
        if (mOpenAnim == null) {
            mOpenAnim = new ExpandCollapseAnimation(this, mCLoseHeight, mOpenHeight);
            mOpenAnim.setFillAfter(true);
            mOpenAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ExpandableTextView.super.setMaxLines(Integer.MAX_VALUE);
                    setText(mOpenSpannableStr);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //  动画结束后textview设置展开的状态
                    getLayoutParams().height = mOpenHeight;
                    requestLayout();
                    animating = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (animating) {
            return;
        }
        animating = true;
        clearAnimation();
        //  执行动画
        startAnimation(mOpenAnim);
    }

    /** 执行收起动画 */
    private void executeCloseAnim() {
        //创建收起动画
        if (mCloseAnim == null) {
            mCloseAnim = new ExpandCollapseAnimation(this, mOpenHeight, mCLoseHeight);
            mCloseAnim.setFillAfter(true);
            mCloseAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animating = false;
                    ExpandableTextView.super.setMaxLines(mMaxLines);
                    setText(mCloseSpannableStr);
                    getLayoutParams().height = mCLoseHeight;
                    requestLayout();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (animating) {
            return;
        }
        animating = true;
        clearAnimation();
        //  执行动画
        startAnimation(mCloseAnim);
    }

    /**
     * @param spannable
     *
     * @return
     */
    private Layout createStaticLayout(SpannableStringBuilder spannable) {
        int contentWidth = initWidth - getPaddingLeft() - getPaddingRight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return new StaticLayout(spannable, getPaint(), contentWidth, Layout.Alignment.ALIGN_NORMAL,
                    getLineSpacingMultiplier(), getLineSpacingExtra(), false);
        }else{
            return new StaticLayout(spannable, getPaint(), contentWidth, Layout.Alignment.ALIGN_NORMAL,
                    1f, 0.0f, false);
        }
    }

    /**
     * @param charSequence
     *
     * @return
     */
    private SpannableStringBuilder charSequenceToSpannable(@NonNull CharSequence charSequence) {
        SpannableStringBuilder spannableStringBuilder = null;
        if (mCharSequenceToSpannableHandler != null) {
            spannableStringBuilder = mCharSequenceToSpannableHandler.charSequenceToSpannable(charSequence);
        }
        if (spannableStringBuilder == null) {
            spannableStringBuilder = new SpannableStringBuilder(charSequence);
        }
        return spannableStringBuilder;
    }

    /**
     * 初始化TextView的可展示宽度
     *
     * @param width
     */
    public void initWidth(int width) {
        initWidth = width;
    }

    @Override
    public void setMaxLines(int maxLines) {
        this.mMaxLines = maxLines;
        super.setMaxLines(maxLines);
    }

    /**
     * 设置展开后缀text
     *
     * @param openSuffix
     */
    public void setOpenSuffix(String openSuffix) {
        mOpenSuffixStr = openSuffix;
        updateOpenSuffixSpan();
    }

    /**
     * 设置展开后缀文本颜色
     *
     * @param openSuffixColor
     */
    public void setOpenSuffixColor(@ColorInt int openSuffixColor) {
        mOpenSuffixColor = openSuffixColor;
        updateOpenSuffixSpan();
    }

    /**
     * 设置收起后缀text
     *
     * @param closeSuffix
     */
    public void setCloseSuffix(String closeSuffix) {
        mCloseSuffixStr = closeSuffix;
        updateCloseSuffixSpan();
    }

    /**
     * 设置收起后缀文本颜色
     *
     * @param closeSuffixColor
     */
    public void setCloseSuffixColor(@ColorInt int closeSuffixColor) {
        mCloseSuffixColor = closeSuffixColor;
        updateCloseSuffixSpan();
    }

    /**
     * 收起后缀是否另起一行
     *
     * @param closeInNewLine
     */
    public void setCloseInNewLine(boolean closeInNewLine) {
        mCloseInNewLine = closeInNewLine;
        updateCloseSuffixSpan();
    }

    /** 更新展开后缀Spannable */
    private void updateOpenSuffixSpan() {
        if (TextUtils.isEmpty(mOpenSuffixStr)) {
            mOpenSuffixSpan = null;
            return;
        }
        mOpenSuffixSpan = new SpannableString(mOpenSuffixStr);
        mOpenSuffixSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, mOpenSuffixStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mOpenSuffixSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                switchOpenClose();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(mOpenSuffixColor);
                ds.setUnderlineText(false);
            }
        },0, mCloseSuffixStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /** 更新收起后缀Spannable */
    private void updateCloseSuffixSpan() {
        if (TextUtils.isEmpty(mCloseSuffixStr)) {
            mCloseSuffixSpan = null;
            return;
        }
        mCloseSuffixSpan = new SpannableString(mCloseSuffixStr);
        mCloseSuffixSpan.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, mCloseSuffixStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (mCloseInNewLine) {
            AlignmentSpan alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
            mCloseSuffixSpan.setSpan(alignmentSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mCloseSuffixSpan.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                switchOpenClose();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(mCloseSuffixColor);
                ds.setUnderlineText(false);
            }
        },1, mCloseSuffixStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public OpenAndCloseCallback mOpenCloseCallback;
    public void setOpenAndCloseCallback(OpenAndCloseCallback callback){
        this.mOpenCloseCallback = callback;
    }

    public interface OpenAndCloseCallback{
        void onOpen();
        void onClose();
    }
    /**
     * 设置文本内容处理
     *
     * @param handler
     */
    public void setCharSequenceToSpannableHandler(CharSequenceToSpannableHandler handler) {
        mCharSequenceToSpannableHandler = handler;
    }

    public interface CharSequenceToSpannableHandler {
        @NonNull
        SpannableStringBuilder charSequenceToSpannable(CharSequence charSequence);
    }

    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;//动画执行view
        private final int mStartHeight;//动画执行的开始高度
        private final int mEndHeight;//动画结束后的高度

        ExpandCollapseAnimation(View target, int startHeight, int endHeight) {
            mTargetView = target;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(400);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mTargetView.setScrollY(0);
            //计算出每次应该显示的高度,改变执行view的高度，实现动画
            mTargetView.getLayoutParams().height = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            mTargetView.requestLayout();
        }
    }
}
