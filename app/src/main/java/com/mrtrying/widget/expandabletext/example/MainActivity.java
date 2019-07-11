package com.mrtrying.widget.expandabletext.example;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.mrtrying.widget.expandabletext.ExpandableTextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExpandableTextView expandableTextView = findViewById(R.id.expanded_text);
        int viewWidth = getWindowManager().getDefaultDisplay().getWidth() - dp2px(this, 20f);
        expandableTextView.initWidth(viewWidth);
        expandableTextView.setMaxLines(3);
        expandableTextView.setHasAnimation(true);
        expandableTextView.setCloseInNewLine(true);
        expandableTextView.setOpenSuffixColor(getResources().getColor(R.color.colorAccent));
        expandableTextView.setCloseSuffixColor(getResources().getColor(R.color.colorAccent));
        expandableTextView.setOriginalText("在全球，随着Flutter被越来越多的知名公司应用在自己的商业APP中，" +
                "Flutter这门新技术也逐渐进入了移动开发者的视野，尤其是当Google在2018年IO大会上发布了第一个" +
                "Preview版本后，国内刮起来一股学习Flutter的热潮。\n\n为了更好的方便帮助中国开发者了解这门新技术" +
                "，我们，Flutter中文网，前后发起了Flutter翻译计划、Flutter开源计划，前者主要的任务是翻译" +
                "Flutter官方文档，后者则主要是开发一些常用的包来丰富Flutter生态，帮助开发者提高开发效率。而时" +
                "至今日，这两件事取得的效果还都不错！"
        );

    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        int res = 0;
        final float scale = context.getResources().getDisplayMetrics().density;
        if (dpValue < 0)
            res = -(int) (-dpValue * scale + 0.5f);
        else
            res = (int) (dpValue * scale + 0.5f);
        return res;
    }
}
