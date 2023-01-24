package hud;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import holders.Configuration;
import holders.CurrentSessionFilesProcessingSettings;
import tools.Logger;
import tools.TextProcessor;
import tools.Utils;

public class SessionSettingsGUI extends JDialog {
	private static final long serialVersionUID = 19652196367521724L;
	
	private boolean disposed = false;
	
	private JRadioButton rdb_upscale_by_ai;
	private JRadioButton rdb_upscale_by_java;
	
	private JComboBox ddl_for_images_scale_modes;
	private JComboBox ddl_for_animations_scale_modes;

	private JLabel lbl_header_images;
	private JLabel lbl_header_animations;
	
	private JCheckBox chkbx_animation_interpolate;
	private JCheckBox chkbx_animation_reset_fps;
	private JCheckBox chkbx_animation_sclae_with_ffmpeg;
	
	private JLabel     lbl_ffmpeg_filters;
	private JTextField txt_ffmpeg_filters;
	
	private JLabel     lbl_ffmpeg_encode_lib;
	private JComboBox  ddl_ffmpeg_encode_lib;
	
	private JLabel lbl_for_images_scale_mode_01;
	private JLabel lbl_for_images_scale_mode_02;
	private JLabel lbl_for_images_scale_mode_03_s;
	private JLabel lbl_for_images_scale_mode_03_m;
	private JLabel lbl_for_images_scale_mode_03_r;
	
	private OnlyNumbersField txt_for_images_scale_mode_01;
	private JComboBox        ddl_for_images_scale_mode_02;
	private OnlyNumbersField txt_for_images_scale_mode_03_w;
	private OnlyNumbersField txt_for_images_scale_mode_03_h;
	
	private JLabel lbl_for_animations_scale_mode_01;
	private JLabel lbl_for_animations_scale_mode_02;
	private JLabel lbl_for_animations_scale_mode_03_s;
	private JLabel lbl_for_animations_scale_mode_03_m;
	private JLabel lbl_for_animations_scale_mode_03_r;
	
	private OnlyNumbersField txt_for_animations_scale_mode_01;
	private JComboBox        ddl_for_animations_scale_mode_02;
	private OnlyNumbersField txt_for_animations_scale_mode_03_w;
	private OnlyNumbersField txt_for_animations_scale_mode_03_h;
	
    private JButton btn_submit;
    private JCheckBox chkbx_prevent_temp_files_delete;

	public SessionSettingsGUI() {
		// make position absolute
		setLayout(null);
		
		String[] items_for_ddl_of_images_scale_modes = {
			"Scale X times",
			"Scale to (preset)",
			"Scale to Custom"
		};
		
		String[] items_for_ddl_of_animations_scale_modes = {
			"Scale X times",
			"Scale to (preset)",
			"Scale to Custom"
		};
		
		String[] items_for_ddl_of_images_scale_mode_02 = {
			"854 x 480 (480p)",
			"1280 x 720 (720p)",
			"1920 x 1080 (1080p)",
			"2560 x 1440 (2k)",
			"3840 x 2160 (4K)"
		};
		
		String[] items_for_ddl_of_animations_scale_mode_02 = {
			"854 x 480 (480p)",
			"1280 x 720 (720p)",
			"1920 x 1080 (1080p)",
			"2560 x 1440 (2k)",
			"3840 x 2160 (4K)"
		};
		
		String[] items_for_ddl_of_ffmpeg_encode_libs = {
			"H.265 CPU Encoding (libx265)",
			"H.265 NVIDIA GPU Encoding (hevc_nvenc)",
			"H.265 AMD GPU Encoding (hevc_amf)",
			"H.264 CPU Encoding (libx264)",
			"H.264 NVIDIA GPU Encoding (h264_nvenc)",
			"H.264 AMD GPU Encoding (h264_amf)"
		};
		
		
		// main DropDownLists
		ddl_for_images_scale_modes = new JComboBox(items_for_ddl_of_images_scale_modes);
		ddl_for_images_scale_modes.setToolTipText("tt_ddl_scale_mode");
		ddl_for_images_scale_modes.setBounds(10, 70, 150, 25);
		add(ddl_for_images_scale_modes);
		ddl_for_images_scale_modes.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { setImageScaligParameters(Configuration.SETTINGS, ddl_for_images_scale_modes.getSelectedIndex()); } });
		
		ddl_for_animations_scale_modes = new JComboBox(items_for_ddl_of_animations_scale_modes);
		ddl_for_animations_scale_modes.setToolTipText("tt_ddl_video_scale_mode");
		ddl_for_animations_scale_modes.setBounds(10, 130, 150, 25);
		add(ddl_for_animations_scale_modes);
		ddl_for_animations_scale_modes.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { setAnimationScaligParameters(Configuration.SETTINGS, ddl_for_animations_scale_modes.getSelectedIndex()); } });

		// FFMPEG libs DropDownList
		ddl_ffmpeg_encode_lib = new JComboBox(items_for_ddl_of_ffmpeg_encode_libs);
		ddl_ffmpeg_encode_lib.setToolTipText("tt_ddl_ffmpeg_encode_lib");
		ddl_ffmpeg_encode_lib.setBounds(155, 185, 250, 25);
		add(ddl_ffmpeg_encode_lib);
		
		
		// Header
		rdb_upscale_by_ai   = new JRadioButton("Upscale by AI");
		rdb_upscale_by_ai.setToolTipText("tt_rdb_upscale_by_ai");
		rdb_upscale_by_ai.setBounds(70, 10, 120, 30);
		add(rdb_upscale_by_ai);
		rdb_upscale_by_ai.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { swapUpscalerOption(false); } });
		
		rdb_upscale_by_java = new JRadioButton("Upscale by Java");
		rdb_upscale_by_java.setToolTipText("tt_rdb_upscale_by_java");
		rdb_upscale_by_java.setBounds(215, 10, 135, 30);
		add(rdb_upscale_by_java);
		rdb_upscale_by_java.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { swapUpscalerOption(true); } });
		
		// Images options
		lbl_header_images = new JLabel("Images:");
		lbl_header_images.setToolTipText("tt_lbl_header_images");
		lbl_header_images.setBounds(10, 50, 60, 20);
		add(lbl_header_images);
		
		// Images Scale Mode 01 (Scale X times)
		lbl_for_images_scale_mode_01 = new JLabel("Scale Factor:");
		lbl_for_images_scale_mode_01.setToolTipText("tt_lbl_for_scale_mode_01");
		lbl_for_images_scale_mode_01.setBounds(170, 70, 100, 25);
		add(lbl_for_images_scale_mode_01);
		
		txt_for_images_scale_mode_01 = new OnlyNumbersField(1).setMaxLength(5);
		txt_for_images_scale_mode_01.setToolTipText("tt_txt_for_scale_mode_01");
		txt_for_images_scale_mode_01.setBounds(265, 70, 45, 25);
		add(txt_for_images_scale_mode_01);
		
		// Images Scale Mode 02 (Scale to preset)
		lbl_for_images_scale_mode_02 = new JLabel("Scale to:");
		lbl_for_images_scale_mode_02.setToolTipText("tt_lbl_for_scale_mode_02");
		lbl_for_images_scale_mode_02.setBounds(170, 70, 65, 25);
		add(lbl_for_images_scale_mode_02);
		
		ddl_for_images_scale_mode_02 = new JComboBox(items_for_ddl_of_images_scale_mode_02);
		ddl_for_images_scale_mode_02.setToolTipText("tt_ddl_for_scale_mode_02");
		ddl_for_images_scale_mode_02.setBounds(235, 70, 170, 25);
		add(ddl_for_images_scale_mode_02);
		
		// Images Scale Mode 03 (Scale to custom)
		lbl_for_images_scale_mode_03_s = new JLabel("Scale to:");
		lbl_for_images_scale_mode_03_s.setToolTipText("tt_lbl_for_scale_mode_03_s");
		lbl_for_images_scale_mode_03_s.setBounds(170, 70, 65, 25);
		add(lbl_for_images_scale_mode_03_s);
		
		txt_for_images_scale_mode_03_w = new OnlyNumbersField(1).setMaxLength(6);
		txt_for_images_scale_mode_03_w.setToolTipText("tt_txt_for_scale_mode_03_w");
		txt_for_images_scale_mode_03_w.setBounds(235, 70, 60, 25);
		add(txt_for_images_scale_mode_03_w);
		
		lbl_for_images_scale_mode_03_m = new JLabel(" x ");
		lbl_for_images_scale_mode_03_m.setToolTipText("tt_lbl_for_scale_mode_03_m");
		lbl_for_images_scale_mode_03_m.setBounds(295, 70, 15, 25);
		add(lbl_for_images_scale_mode_03_m);
		
		txt_for_images_scale_mode_03_h = new OnlyNumbersField(1).setMaxLength(6);
		txt_for_images_scale_mode_03_h.setToolTipText("tt_txt_for_scale_mode_03_h");
		txt_for_images_scale_mode_03_h.setBounds(310, 70, 60, 25);
		add(txt_for_images_scale_mode_03_h);
		
		lbl_for_images_scale_mode_03_r = new JLabel(""); // 16:10
		lbl_for_images_scale_mode_03_r.setToolTipText("tt_lbl_for_scale_mode_03_e_ratio");
		lbl_for_images_scale_mode_03_r.setBounds(370, 70, 40, 25);
		add(lbl_for_images_scale_mode_03_r);

		
		// Animations options
		lbl_header_animations = new JLabel("Video and Animations (GIF/APNG/WEBP):");
		lbl_header_animations.setToolTipText("tt_lbl_header_video");
		lbl_header_animations.setBounds(10, 110, 295, 20);
		add(lbl_header_animations);
		
		// Animation Interpolation options
		chkbx_animation_interpolate = new JCheckBox("Interpolate");
		chkbx_animation_interpolate.setToolTipText("tt_chk_interpolate");
		chkbx_animation_interpolate.setBounds(10, 160, 105, 25);
		add(chkbx_animation_interpolate);
		chkbx_animation_interpolate.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { chkbx_animation_reset_fps.setEnabled(chkbx_animation_interpolate.isSelected()); } });
		
		chkbx_animation_reset_fps = new JCheckBox("Reset FPS");
		chkbx_animation_reset_fps.setToolTipText("tt_chb_reset_fps");
		chkbx_animation_reset_fps.setBounds(115, 160, 100, 25);
		add(chkbx_animation_reset_fps);
		
		// Other FFMPEG Animation Options
		chkbx_animation_sclae_with_ffmpeg = new JCheckBox("Scale with FFMPEG");
		chkbx_animation_sclae_with_ffmpeg.setToolTipText("tt_chx_sclae_with_ffmpeg");
		chkbx_animation_sclae_with_ffmpeg.setBounds(215, 160, 160, 25);
		add(chkbx_animation_sclae_with_ffmpeg);
		
		lbl_ffmpeg_encode_lib = new JLabel("FFMPEG Encode Lib:");
		lbl_ffmpeg_encode_lib.setToolTipText("tt_lbl_ffmpeg_encode_lib");
		lbl_ffmpeg_encode_lib.setBounds(15, 185, 145, 25);
		add(lbl_ffmpeg_encode_lib);
		
		lbl_ffmpeg_filters = new JLabel("Other FFMPEG Filters:");
		lbl_ffmpeg_filters.setToolTipText("tt_lbl_ffmpeg_filters");
		lbl_ffmpeg_filters.setBounds(10, 210, 160, 25);
		add(lbl_ffmpeg_filters);
		
		txt_ffmpeg_filters = new JTextField(20);
		txt_ffmpeg_filters.setToolTipText("tt_txt_ffmpeg_filters");
		txt_ffmpeg_filters.setBounds(10, 230, 395, 25);
		add(txt_ffmpeg_filters);
		
		// Animations Scale Mode 01 (Scale X times)
		lbl_for_animations_scale_mode_01 = new JLabel("Scale Factor:");
		lbl_for_animations_scale_mode_01.setToolTipText("tt_lbl_video_scale_mode_01");
		lbl_for_animations_scale_mode_01.setBounds(170, 130, 95, 25);
		add(lbl_for_animations_scale_mode_01);
		
		txt_for_animations_scale_mode_01 = new OnlyNumbersField(1).setMaxLength(5);
		txt_for_animations_scale_mode_01.setToolTipText("tt_lbl_video_scale_mode_01");
		txt_for_animations_scale_mode_01.setBounds(265, 130, 45, 25);
		add(txt_for_animations_scale_mode_01);
		
		// Animations Scale Mode 02 (Scale to preset)
		lbl_for_animations_scale_mode_02 = new JLabel("Scale to:");
		lbl_for_animations_scale_mode_02.setToolTipText("tt_lbl_video_scale_mode_01");
		lbl_for_animations_scale_mode_02.setBounds(170, 130, 65, 25);
		add(lbl_for_animations_scale_mode_02);
		
		ddl_for_animations_scale_mode_02 = new JComboBox(items_for_ddl_of_animations_scale_mode_02);
		ddl_for_animations_scale_mode_02.setToolTipText("tt_ddl_for_video_scale_mode_02");
		ddl_for_animations_scale_mode_02.setBounds(235, 130, 170, 25);
		add(ddl_for_animations_scale_mode_02);
		
		// Animations Scale Mode 03 (Scale to custom)
		lbl_for_animations_scale_mode_03_s = new JLabel("Scale to:");
		lbl_for_animations_scale_mode_03_s.setToolTipText("tt_lbl_video_scale_mode_03_s");
		lbl_for_animations_scale_mode_03_s.setBounds(170, 130, 70, 25);
		add(lbl_for_animations_scale_mode_03_s);
		
		txt_for_animations_scale_mode_03_w = new OnlyNumbersField(1).setMaxLength(6);
		txt_for_animations_scale_mode_03_w.setToolTipText("tt_txt_for_video_scale_mode_03_w");
		txt_for_animations_scale_mode_03_w.setBounds(235, 130, 60, 25);
		add(txt_for_animations_scale_mode_03_w);
		
		lbl_for_animations_scale_mode_03_m = new JLabel(" x ");
		lbl_for_animations_scale_mode_03_m.setToolTipText("tt_lbl_video_scale_mode_03_m");
		lbl_for_animations_scale_mode_03_m.setBounds(295, 130, 15, 25);
		add(lbl_for_animations_scale_mode_03_m);
		
		txt_for_animations_scale_mode_03_h = new OnlyNumbersField(1).setMaxLength(6);
		txt_for_animations_scale_mode_03_h.setToolTipText("tt_txt_for_video_scale_mode_03_h");
		txt_for_animations_scale_mode_03_h.setBounds(310, 130, 60, 25);
		add(txt_for_animations_scale_mode_03_h);
		
		lbl_for_animations_scale_mode_03_r = new JLabel(""); // 16:40
		lbl_for_animations_scale_mode_03_r.setToolTipText("tt_lbl_video_scale_mode_03_e");
		lbl_for_animations_scale_mode_03_r.setBounds(370, 130, 40, 25);
		add(lbl_for_animations_scale_mode_03_r);
        
        chkbx_prevent_temp_files_delete = new JCheckBox("Do NOT remove Temp Files");
        chkbx_prevent_temp_files_delete.setToolTipText("tt_chkbx_prevent_temp_files_delete");
        chkbx_prevent_temp_files_delete.setBounds(10, 270, 215, 25);
        add(chkbx_prevent_temp_files_delete);
        
        btn_submit = new JButton("OK");
        btn_submit.setToolTipText("tt_btn_submit");
        btn_submit.setBounds(340, 265, 65, 30);
        add(btn_submit);
        btn_submit.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { dispose(); } } );

		
		// set window options
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		 
		// adjust size and set layout
		pack();
		//setPreferredSize(new Dimension(412, 266));
		setSize(412,332);
		setLocationRelativeTo(null);
        
        // Show the pop-up window
		initOptions();
        setVisible(true);
	}
	
	private void initOptions() {
		rdb_upscale_by_ai.setSelected(false);
		rdb_upscale_by_java.setSelected(false);
		
		boolean upscaleAvaible = (Configuration.OPTIONS != null && Configuration.OPTIONS.canUpscale());
		boolean interpolateAvaible = (Configuration.OPTIONS != null && Configuration.OPTIONS.canSmooth());
		
		CurrentSessionFilesProcessingSettings settings = Configuration.SETTINGS;
		if(settings != null) {
			
			if(upscaleAvaible) {
				if(settings.userWantUpscale) {
					rdb_upscale_by_ai.setSelected(true);
				}
				else {
					rdb_upscale_by_java.setSelected(true);
				}
			}
			else {
				rdb_upscale_by_java.setSelected(true);
				rdb_upscale_by_ai.setEnabled(false);
			}
			
			chkbx_animation_interpolate.setSelected(interpolateAvaible && settings.userWantInterpolate);
			chkbx_animation_interpolate.setEnabled(interpolateAvaible);
			
			chkbx_animation_reset_fps.setSelected(settings.userWantRestoreFrameRate);
			chkbx_animation_reset_fps.setEnabled(interpolateAvaible && chkbx_animation_interpolate.isSelected());
			
			chkbx_animation_sclae_with_ffmpeg.setSelected(settings.scaleWithFFMPEG);
			
			txt_ffmpeg_filters.setText(settings.customFFMPEGFilterCMD);
			
			chkbx_prevent_temp_files_delete.setSelected(!settings.removeTempFiles);
		}
		else { // default values
			if(upscaleAvaible) {
				rdb_upscale_by_ai.setSelected(true);
			}
			else {
				rdb_upscale_by_java.setSelected(true);
				rdb_upscale_by_ai.setEnabled(false);
			}
			
			chkbx_animation_interpolate.setSelected(interpolateAvaible);
			chkbx_animation_interpolate.setEnabled(interpolateAvaible);
			
			chkbx_animation_reset_fps.setSelected(false);
			chkbx_animation_reset_fps.setEnabled(interpolateAvaible && chkbx_animation_interpolate.isSelected());
			
			chkbx_animation_sclae_with_ffmpeg.setSelected(false);
			
			txt_ffmpeg_filters.setText("");
			
			chkbx_prevent_temp_files_delete.setSelected(!TextProcessor.parseBoolean(Configuration.getVAR("REMOVE_TEMP_FILES")));
		}
		
		setImageScaligParameters     (settings, -1);
		setAnimationScaligParameters (settings, -1);
	}
	
	private void setAnimationScaligParameters(CurrentSessionFilesProcessingSettings settings, int outopt) {
		
		int mopt = ((settings == null) ? 0 : settings.animationScalingOption);
		int sopt = ((settings == null) ? 2 : settings.animationPresetOption);
		String w = ((settings == null) ? "2560" : ("" + settings.animationTargetW));
		String h = ((settings == null) ? "1440" : ("" + settings.animationTargetH));
		String s = ((settings == null) ?    "2" : ("" + settings.animationScalingFactor));
		
		if((outopt == 1) || (mopt == 1 && outopt < 0)) {
			ddl_for_animations_scale_modes.setSelectedIndex(1);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_animations_scale_mode_01.setVisible(false);
			txt_for_animations_scale_mode_01.setVisible(false);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_animations_scale_mode_02.setVisible(true);
			ddl_for_animations_scale_mode_02.setVisible(true);
			ddl_for_animations_scale_mode_02.setSelectedIndex(sopt);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_animations_scale_mode_03_s.setVisible(false);
			txt_for_animations_scale_mode_03_w.setVisible(false);
			lbl_for_animations_scale_mode_03_m.setVisible(false);
			txt_for_animations_scale_mode_03_h.setVisible(false);
			lbl_for_animations_scale_mode_03_r.setVisible(false);
		}
		else if((outopt == 2) || (mopt == 2 && outopt < 0)) {
			ddl_for_animations_scale_modes.setSelectedIndex(2);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_animations_scale_mode_01.setVisible(false);
			txt_for_animations_scale_mode_01.setVisible(false);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_animations_scale_mode_02.setVisible(false);
			ddl_for_animations_scale_mode_02.setVisible(false);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_animations_scale_mode_03_s.setVisible(true);
			txt_for_animations_scale_mode_03_w.setVisible(true);
			lbl_for_animations_scale_mode_03_m.setVisible(true);
			txt_for_animations_scale_mode_03_h.setVisible(true);
			lbl_for_animations_scale_mode_03_r.setVisible(true);
			txt_for_animations_scale_mode_03_w.setText(w);
			txt_for_animations_scale_mode_03_h.setText(h);
		}
		else {
			ddl_for_animations_scale_modes.setSelectedIndex(0);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_animations_scale_mode_01.setVisible(true);
			txt_for_animations_scale_mode_01.setVisible(true);
			txt_for_animations_scale_mode_01.setText(s);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_animations_scale_mode_02.setVisible(false);
			ddl_for_animations_scale_mode_02.setVisible(false);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_animations_scale_mode_03_s.setVisible(false);
			txt_for_animations_scale_mode_03_w.setVisible(false);
			lbl_for_animations_scale_mode_03_m.setVisible(false);
			txt_for_animations_scale_mode_03_h.setVisible(false);
			lbl_for_animations_scale_mode_03_r.setVisible(false);
		}
	}
	
	private void setImageScaligParameters(CurrentSessionFilesProcessingSettings settings, int outopt) {
		
		int mopt = ((settings == null) ? 0 : settings.imageScalingOption);
		int sopt = ((settings == null) ? 3 : settings.imagePresetOption);
		String w = ((settings == null) ? "2560" : ("" + settings.imageTargetW));
		String h = ((settings == null) ? "1440" : ("" + settings.imageTargetH));
		String s = ((settings == null) ?    "2" : ("" + settings.imageScalingFactor));
		
		if((outopt == 1) || (mopt == 1 && outopt < 0)) {
			ddl_for_images_scale_modes.setSelectedIndex(1);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_images_scale_mode_01.setVisible(false);
			txt_for_images_scale_mode_01.setVisible(false);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_images_scale_mode_02.setVisible(true);
			ddl_for_images_scale_mode_02.setVisible(true);
			ddl_for_images_scale_mode_02.setSelectedIndex(sopt);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_images_scale_mode_03_s.setVisible(false);
			txt_for_images_scale_mode_03_w.setVisible(false);
			lbl_for_images_scale_mode_03_m.setVisible(false);
			txt_for_images_scale_mode_03_h.setVisible(false);
			lbl_for_images_scale_mode_03_r.setVisible(false);
		}
		else if((outopt == 2) || (mopt == 2 && outopt < 0)) {
			ddl_for_images_scale_modes.setSelectedIndex(2);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_images_scale_mode_01.setVisible(false);
			txt_for_images_scale_mode_01.setVisible(false);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_images_scale_mode_02.setVisible(false);
			ddl_for_images_scale_mode_02.setVisible(false);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_images_scale_mode_03_s.setVisible(true);
			txt_for_images_scale_mode_03_w.setVisible(true);
			lbl_for_images_scale_mode_03_m.setVisible(true);
			txt_for_images_scale_mode_03_h.setVisible(true);
			lbl_for_images_scale_mode_03_r.setVisible(true);
			txt_for_images_scale_mode_03_w.setText(w);
			txt_for_images_scale_mode_03_h.setText(h);
		}
		else {
			ddl_for_images_scale_modes.setSelectedIndex(0);
			// Images Scale Mode 01 (Scale X times)
			lbl_for_images_scale_mode_01.setVisible(true);
			txt_for_images_scale_mode_01.setVisible(true);
			txt_for_images_scale_mode_01.setText(s);
			
			// Images Scale Mode 02 (Scale to preset)
			lbl_for_images_scale_mode_02.setVisible(false);
			ddl_for_images_scale_mode_02.setVisible(false);
			
			// Images Scale Mode 03 (Scale to custom)
			lbl_for_images_scale_mode_03_s.setVisible(false);
			txt_for_images_scale_mode_03_w.setVisible(false);
			lbl_for_images_scale_mode_03_m.setVisible(false);
			txt_for_images_scale_mode_03_h.setVisible(false);
			lbl_for_images_scale_mode_03_r.setVisible(false);
		}
	}
	
	private void swapUpscalerOption(boolean simple) {
		if(!simple && Configuration.OPTIONS != null && Configuration.OPTIONS.canUpscale()) {
			rdb_upscale_by_ai.setSelected(true);
			rdb_upscale_by_java.setSelected(false);
		}
		else {
			rdb_upscale_by_ai.setSelected(false);
			rdb_upscale_by_java.setSelected(true);
		}
	}
	
	@Override
    public void dispose() {
		if(!disposed) {
			
			boolean userWantInterpolate = chkbx_animation_interpolate.isSelected();
			boolean userWantUpscale     = rdb_upscale_by_ai.isSelected();
			boolean restoreFrameRate    = chkbx_animation_reset_fps.isSelected();
			boolean upscaleWithJava     = rdb_upscale_by_java.isSelected();
			boolean scaleWithFFMPEG     = chkbx_animation_sclae_with_ffmpeg.isSelected();
			
			boolean removeTempFiles     = !chkbx_prevent_temp_files_delete.isSelected();
			
			int imgScaleOpt  = ddl_for_images_scale_modes.getSelectedIndex();
			int imgPresetOpt = ddl_for_images_scale_mode_02.getSelectedIndex();
			
			int imgSF = Utils.clamp(2, 128, TextProcessor.stringToInt(txt_for_images_scale_mode_01.getText(), 4));
			int imgW  = 2560;
			int imgH  = 1440;
			
			if(imgScaleOpt == 1) {
				switch (imgScaleOpt) {
					case  0 : imgW =  854; imgH =  480; break;
					case  1 : imgW = 1280; imgH =  720; break;
					case  2 : imgW = 1920; imgH = 1080; break;
					case  3 : imgW = 2560; imgH = 1440; break;
					case  4 : imgW = 3840; imgH = 2160; break;
					default : imgW = 1920; imgH = 1080; break;
				}
			}
			else if(imgScaleOpt == 2) {
				imgW = Utils.clamp(2, 999998, TextProcessor.stringToInt(txt_for_images_scale_mode_03_w.getText(), 2560));
				imgH = Utils.clamp(2, 999998, TextProcessor.stringToInt(txt_for_images_scale_mode_03_h.getText(), 1440));
			}
			
			int animScaleOpt  = ddl_for_animations_scale_modes.getSelectedIndex();
			int animPresetOpt = ddl_for_animations_scale_mode_02.getSelectedIndex();
			
			int animSF = Utils.clamp(2, 32, TextProcessor.stringToInt(txt_for_animations_scale_mode_01.getText(), 2));
			int animW  = 1920;
			int animH  = 1080;
			
			if(animScaleOpt == 1) {
				switch (animPresetOpt) {
					case  0 : animW =  854; animH =  480; break;
					case  1 : animW = 1280; animH =  720; break;
					case  2 : animW = 1920; animH = 1080; break;
					case  3 : animW = 2560; animH = 1440; break;
					case  4 : animW = 3840; animH = 2160; break;
					default : animW = 1920; animH = 1080; break;
				}
			}
			else if(animScaleOpt == 2) {
				animW = Utils.clamp(2, 999998, TextProcessor.stringToInt(txt_for_animations_scale_mode_03_w.getText(), 1920));
				animH = Utils.clamp(2, 999998, TextProcessor.stringToInt(txt_for_animations_scale_mode_03_h.getText(), 1080));
			}
			
			Configuration.SETTINGS = new CurrentSessionFilesProcessingSettings(removeTempFiles, userWantInterpolate, userWantUpscale, restoreFrameRate, upscaleWithJava, scaleWithFFMPEG, txt_ffmpeg_filters.getText(), imgScaleOpt, imgPresetOpt, imgSF, imgW, imgH, animScaleOpt, animPresetOpt, animSF, animW, animH);
			
			if(!Configuration.SETTINGS.toFile()) {
				System.err.println("Can't save user settings file: " + Configuration.SESSION_CONFIG_FILE_PATH);
			}
	
	        // don't forget to call the dispose method of the super class
	        // to ensure that the dialog is properly disposed
	        super.dispose();
	        disposed = true;
		}
    }
	
	class OnlyNumbersField extends JTextField {
	    private static final long serialVersionUID = 2982053768962356662L;
	    private int fieldLimit = -1;
		public OnlyNumbersField(int rows) {
	        super(rows);
	        this.addKeyListener(new KeyAdapter() {
	            public void keyTyped(KeyEvent e) {
	                char c = e.getKeyChar();
	                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
	                    e.consume();
	                }
	                else {
	                	String selectedText = getSelectedText();
	                	boolean nothingSelected = !(selectedText != null && (selectedText.length() > 0));
	                	if((fieldLimit > 0) && ((getText().length()) >= fieldLimit) && nothingSelected) {
	                		e.consume();
	                	}
	                }
	            }
	        });
	    }
		
		private OnlyNumbersField setMaxLength(int limit) {
			this.fieldLimit = limit;
			return this;
		}
	}
}
