package com.af.pxl;

import android.app.Activity;
import android.content.Context;
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

public class ToolSettingsManager {

    private FrameLayout toolSettingsWindowLayout;
    private AdaptivePixelSurfaceH aps;
    private AdaptivePixelSurfaceH.Tool currentTool;
    private Context c;

    private String size;

    ToolSettingsManager(Activity drawingActivity, AdaptivePixelSurfaceH aps){
        this.aps = aps;
        toolSettingsWindowLayout = (FrameLayout) drawingActivity.findViewById(R.id.toolSettings);
        c = drawingActivity;
        size = c.getString(R.string.stroke_width);
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
    private TextView toolSizeText;
    private SeekBar sizeBar;
    private void initializePencilView(){
        pencilView = LayoutInflater.from(c).inflate(R.layout.tool_settings_pencil, toolSettingsWindowLayout, false);

        toolSizeText  = (TextView) pencilView.findViewById(R.id.toolSizeText);
        toolSizeText.setText(size+": "+ strokeWidth + "px");

        sizeBar = (SeekBar) pencilView.findViewById(R.id.toolSizeSeekBar);
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                strokeWidth = i+1;
                toolSizeText.setText(size+": "+ strokeWidth + "px");
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

        RadioGroup styles = (RadioGroup) pencilView.findViewById(R.id.toolStyleRadioGroup);
        styles.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.toolStyleRadioButtonSquare:
                        aps.superPencil.setStyle(SuperPencilH.Style.SQUARE);
                        break;
                    case R.id.toolStyleRadioButtonRound:
                        aps.superPencil.setStyle(SuperPencilH.Style.ROUND);
                        break;
                }
            }
        });
    }

    private ImageButton line;
    private ImageButton rect;
    private ImageButton circle;
    private Switch lockedSwitch;
    private Switch fillSwitch;
    private SeekBar shapeSizeBar;
    private TextView shapeSizeText;

    private boolean fillShapes = false;

    private void initializeMultishapeView(){
        multishapeView = LayoutInflater.from(c).inflate(R.layout.tool_settings_multishape, toolSettingsWindowLayout, false);

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

                        lockedSwitch.setVisibility(View.GONE);
                        fillSwitch.setVisibility(View.GONE);
                        lockedSwitch.setText(c.getString(R.string.lock_angles));
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
                        break;
                    case R.id.circleTool:
                        if(aps.multiShape.shape== MultiShapeH.Shape.CIRCLE)
                            return;

                        aps.multiShape.shape = MultiShapeH.Shape.CIRCLE;
                        circle.setBackgroundResource(R.drawable.full_round_rect_bg_dark);
                        rect.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);
                        line.setBackgroundResource(R.drawable.sketchbook_style_bg_selector_2);

                        lockedSwitch.setVisibility(View.VISIBLE);
                        fillSwitch.setVisibility(View.VISIBLE);
                        lockedSwitch.setText(c.getString(R.string.lock_circle));
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
        shapeSizeText.setText(size+": "+ strokeWidth + "px");

        shapeSizeBar = (SeekBar) multishapeView.findViewById(R.id.shapeToolSeekbar);
        shapeSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                strokeWidth = i+1;
                shapeSizeText.setText(size+": "+ strokeWidth + "px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                aps.setStrokeWidth(strokeWidth);
            }
        });

        fillSwitch = (Switch) multishapeView.findViewById(R.id.shapeFillSwitch);

        fillSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                aps.multiShape.fill = b;
            }
        });

    }
}
