package com.ssafy.socks.model.magazine;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemesAndCategory {
	private Long ThemeId;
	private List<String> hexColor;
	private String category;
}
