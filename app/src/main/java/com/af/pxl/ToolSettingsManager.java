package com.af.pxl;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by Aefyr on 31.07.2017.
 */

class ToolSettingsManager {

    private FrameLayout toolSettingsWindowLayout;
    private AdaptivePixelSurfaceH aps;
    private AdaptivePixelSurfaceH.Tool currentTool;
    private Context c;

    private String size;
    private String roundingS;
    private RadioGroup.OnCheckedChangeListener radioListener;

    ToolSettingsManager(Activity drawingActivity, AdaptivePixelSurfaceH aps){
        this.aps = aps;
        toolSettingsWindowLayout = (FrameLayout) drawingActivity.findViewById(R.id.toolSettings);
        c = drawingActivity;
        size = c.getString(R.string.stroke_width);
        roundingS = c.getString(R.string.corners_rounding);

        radioListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.toolStyleRadioButtonSquare:
                        if(currentTool == AdaptivePixelSurfaceH.Tool.MULTISHAPE)
                            pencilCapStyles.check(R.id.toolStyleRadioButtonSquare);
                        else
                            shapeCapStyles.check(R.id.toolStyleRadioButtonSquare);

                        ToolSettingsManager.this.aps.superPencil.setStyle(SuperPencilH.Style.SQUARE);
                        break;
                    case R.id.toolStyleRadioButtonRound:
                        if(currentTool == AdaptivePixelSurfaceH.Tool.MULTISHAPE)
                            pencilCapStyles.check(R.id.toolStyleRadioButtonRound);
                        else
                            shapeCapStyles.check(R.id.toolStyleRadioButtonRound);

                        ToolSettingsManager.this.aps.superPencil.setStyle(SuperPencilH.Style.ROUND);
                        break;
                }
            }
        };
    }

    void hide(){
        toolSettingsWindowLayout.setVisibility(View.GONE);
    }

    void show(){
        if(currentTool== AdaptivePixelSurfaceH.Tool.PENCIL||currentTool== AdaptivePixelSurfaceH.Tool.ERASER||currentTool== AdaptivePixelSurfaceH.Tool.MULTISHAPE)
            toolSettingsWindowLayout.setVisibility(View.VISIBLE);
    }

    void notifyToolPicked(AdaptivePixelSurfaceH.Tool tool){
        currentTool = tool;
        switch (currentTool){

            case ERASER:
            case PENCIL:
                pencilSetup();
                show();
                break;
            case FLOOD_FILL:
            case COLOR_PICK:
            case COLOR_SWAP:
                hide();
                break;
            case MULTISHAPE:
                multishapeSetup();
                show();
                break;
        }
    }

    private View pencilView;
    private void pencilSetup(){
        toolSettingsWindowLayout.removeAllViews();
        if(pencilView == null)
            initializePencilView();
        toolSettingsWindowLayout.addView(pencilView);

        sizeBar.setProgress(strokeWidth-1);
    }

    private View multishapeView;
    private void multishapeSetup(){
        toolSettingsWindowLayout.removeAllViews();
        if(multishapeView == null)
            initializeMultishapeView();
        toolSettingsWindowLayout.addView(multishapeView);

        shapeSizeBar.setProgress(strokeWidth-1);
    }

    private int strokeWidth = 1;
    private int rounding = 0;
    private TextView toolSizeText;
    private SeekBar sizeBar;
    private RadioGroup pencilCapStyles;
    private void initializePencilView(){
        pencilView = LayoutInflater.from(c).inflate(R.layout.tool_settings_pencil, toolSettingsWindowLayout, false);

        toolSizeText  = (TextView) pencilView.findViewById(R.id.toolSizeText);
        toolSizeText.setText(String.format(size, strokeWidth));

        sizeBar = (SeekBar) pencilView.findViewById(R.id.toolSizeSeekBar);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                strokeWidth = i+1;
                toolSizeText.setText(String.format(size, strokeWidth));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                aps.setStrokeWidth(strokeWidth);
                if(aps.cursorMode)
                    aps.invalidate();
            }
        });

        pencilCapStyles = (RadioGroup) pencilView.findViewById(R.id.toolStyleRadioGroup);
        pencilCapStyles.setOnCheckedChangeListener(radioListener);
    }

    private ImageButton line;
    private ImageButton rect;
    private ImageButton circle;
    private Switch lockedSwitch;
    private Switch fillSwitch;
    private SeekBar shapeSizeBar;
    private TextView shapeSizeText;
    private TextView toolStyleText;
    private RadioGroup shapeCapStyles;
    private TextView roundingText;
    private SeekBar roundingSeekBar;

    private void initializeMultishapeView(){
        multishapeView = LayoutInflater.from(c).inflate(R.layout.tool_settings_multishape, toolSettingsWindowLayout, false);

        toolStyleText = (TextView) multishapeView.findViewById(R.id.toolStyleText);
        shapeCapStyles = (RadioGroup) multishapeView.findViewById(R.id.toolStyleRadioGroup);

        shapeCapStyles.setOnCheckedChangeListener(radioListener);

        line = (ImageButton) multishapeView.findViewById(R.id.lineTool);
        rect = (ImageButton) multishapeView.findViewById(R.id.rectTool);
        circle = (ImageButton) multishapeView.findViewById(R.id.circleTool);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.lineTool:
                        if(aps.multiShape.shape== MultiShapeH.Shape.LINE)
                            return;

                        aps.multiShape.shape = MultiShapeH.Shape.LINE;
                        line.setBackgroundResource(R.drawable.full_round_rect_bg_dark);
                        rect.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
                        circle.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);

                        lockedSwitch.setVisibility(View.VISIBLE);
                        fillSwitch.setVisibility(View.GONE);
                        lockedSwitch.setText(c.getString(R.string.lock_angles));

                        shapeCapStyles.setVisibility(View.VISIBLE);
                        toolStyleText.setVisibility(View.VISIBLE);

                        if(Build.VERSION.SDK_INT>=21) {
                            roundingText.setVisibility(View.GONE);
                            roundingSeekBar.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.rectTool:
                        if(aps.multiShape.shape== MultiShapeH.Shape.RECT)
                            return;

                        aps.multiShape.shape = MultiShapeH.Shape.RECT;
                        rect.setBackgroundResource(R.drawable.full_round_rect_bg_dark);
                        line.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
                        circle.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);

                        lockedSwitch.setVisibility(View.VISIBLE);
                        fillSwitch.setVisibility(View.VISIBLE);
                        lockedSwitch.setText(c.getString(R.string.lock_square));

                        shapeCapStyles.setVisibility(View.GONE);
                        toolStyleText.setVisibility(View.GONE);

                        if(Build.VERSION.SDK_INT>=21) {
                            roundingText.setVisibility(View.VISIBLE);
                            roundingSeekBar.setVisibility(View.VISIBLE);
                        }
                        break;
                    case R.id.circleTool:
                        if(aps.multiShape.shape== MultiShapeH.Shape.CIRCLE)
                            return;

                        aps.multiShape.shape = MultiShapeH.Shape.CIRCLE;
                        circle.setBackgroundResource(R.drawable.full_round_rect_bg_dark);
                        rect.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
                        line.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);

                        fillSwitch.setVisibility(View.VISIBLE);
                        lockedSwitch.setText(c.getString(R.string.lock_circle));

                        shapeCapStyles.setVisibility(View.GONE);
                        toolStyleText.setVisibility(View.GONE);

                        if(Build.VERSION.SDK_INT>=21) {
                            lockedSwitch.setVisibility(View.VISIBLE);
                            roundingText.setVisibility(View.GONE);
                            roundingSeekBar.setVisibility(View.GONE);
                        }else
                            lockedSwitch.setVisibility(View.GONE);
                        break;
                }
            }
        };

        line.setOnClickListener(listener);
        rect.setOnClickListener(listener);
        circle.setOnClickListener(listener);

        lockedSwitch = (Switch) multishapeView.findViewById(R.id.lockedSwitch);
        lockedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                aps.multiShape.locked = b;
            }
        });

        shapeSizeText = (TextView) multishapeView.findViewById(R.id.shapeToolSizeText);
        shapeSizeText.setText(String.format(size, strokeWidth));

        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(seekBar.getId()==R.id.shapeToolSizeSeekbar) {
                    strokeWidth = i + 1;
                    shapeSizeText.setText(String.format(size, strokeWidth));
                }else {
                    rounding = i;
                    roundingText.setText(String.format(roundingS, rounding));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getId()==R.id.shapeToolSizeSeekbar)
                    aps.setStrokeWidth(strokeWidth);
                else
                    aps.multiShape.rounding = rounding*2;
            }
        };

        shapeSizeBar = (SeekBar) multishapeView.findViewById(R.id.shapeToolSizeSeekbar);
        shapeSizeBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        fillSwitch = (Switch) multishapeView.findViewById(R.id.shapeFillSwitch);

        fillSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                aps.multiShape.fill = b;
            }
        });

        roundingSeekBar = (SeekBar) multishapeView.findViewById(R.id.cornersRoundingSeekBar);
        roundingSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        roundingText = (TextView) multishapeView.findViewById(R.id.roundingText);
        roundingText.setText(String.format(roundingS, rounding));

    }
}
