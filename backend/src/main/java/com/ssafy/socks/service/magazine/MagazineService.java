package com.ssafy.socks.service.magazine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ssafy.socks.advice.exception.CCommunicationException;
import com.ssafy.socks.advice.exception.CMagazineNotFoundException;
import com.ssafy.socks.advice.exception.CUserNotFoundException;
import com.ssafy.socks.entity.color.ColorHistory;
import com.ssafy.socks.entity.magazine.Contents;
import com.ssafy.socks.entity.magazine.Likes;
import com.ssafy.socks.entity.magazine.Magazine;
import com.ssafy.socks.entity.user.User;
import com.ssafy.socks.model.magazine.ContentsModel;
import com.ssafy.socks.model.magazine.LikesModel;
import com.ssafy.socks.model.magazine.MagazineModel;
import com.ssafy.socks.repository.color.ColorHistoryJpaRepository;
import com.ssafy.socks.repository.color.SelectedColorJpaRepository;
import com.ssafy.socks.repository.magazine.BookmarkRepository;
import com.ssafy.socks.repository.magazine.ContentsJpaRepository;
import com.ssafy.socks.repository.magazine.LikesJpaRepository;
import com.ssafy.socks.repository.magazine.MagazineJpaRepository;
import com.ssafy.socks.repository.magazine.MagazineRepository;
import com.ssafy.socks.repository.theme.ThemeJpaRepository;
import com.ssafy.socks.repository.user.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MagazineService {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final MagazineJpaRepository magazineJpaRepository;
	private final UserJpaRepository userJpaRepository;
	private final MagazineRepository magazineRepository;
	private final LikesJpaRepository likesJpaRepository;
	private final BookmarkRepository bookmarkRepository;
	private final ThemeJpaRepository themeJpaRepository;
	private final ColorHistoryJpaRepository colorHistoryJpaRepository;
	private final SelectedColorJpaRepository selectedColorJpaRepository;
	private final ContentsJpaRepository contentsJpaRepository;

	public void saveMagazine(MagazineModel magazineModel) {
		LocalDateTime currDate = LocalDateTime.now();
		User user = userJpaRepository.findByEmail(magazineModel.getEmail()).orElseThrow(CUserNotFoundException::new);

		Magazine magazine = Magazine.builder()
			.userId(user.getId())
			.magazineName(magazineModel.getMagazineName())
			.themeId(magazineModel.getThemeId())
			.selectedId(magazineModel.getSelectedColorId())
			.createdDate(currDate)
			.likeCount(0)
			.build();

		logger.info("----------------- magazine -----------------");
		logger.info("themeId : " + magazine.getThemeId());
		logger.info("current Date : " + magazine.getCreatedDate());
		logger.info("----------------- magazine -----------------");

		ColorHistory colorHistory = new ColorHistory();
		colorHistory.setSelectedColorId(magazineModel.getSelectedColorId());
		colorHistory.setUserId(user.getId());
		colorHistoryJpaRepository.save(colorHistory);

		magazineJpaRepository.save(magazine);

		List<Contents> contentsList = new ArrayList<>();
		for (int i = 0; i < magazineModel.getContents().size(); i++) {
			Contents contents = Contents.builder()
				.url(magazineModel.getContents().get(i).getUrl())
				.answer(magazineModel.getContents().get(i).getAnswer())
				.mainText(magazineModel.getContents().get(i).getMainText())
				.subText(magazineModel.getContents().get(i).getSubText())
				.template(magazineModel.getContents().get(i).getTemplate())
				.question(magazineModel.getContents().get(i).getQuestion())
				.build();
			contentsList.add(contents);
		}

		for (Contents contents : contentsList) {
			contents.setMagazineId(magazine.getId());
			contentsJpaRepository.save(contents);
		}
	}

	public List<MagazineModel> getMagazinesByUser(String userEmail) {
		User user = userJpaRepository.findByEmail(userEmail).orElseThrow(CCommunicationException::new);
		List<Magazine> magazineList = magazineJpaRepository.findByUserId(user.getId());
		List<MagazineModel> magazineModels = new ArrayList<>();

		for(Magazine magazine : magazineList) {
			List<Contents> contentsList = contentsJpaRepository.findByMagazineId(magazine.getId());
			List<ContentsModel> contentsModels = new ArrayList<>();

			for(Contents contents : contentsList) {
				ContentsModel contentsModel = ContentsModel.builder()
					.answer(contents.getAnswer())
					.mainText(contents.getMainText())
					.question(contents.getQuestion())
					.subText(contents.getSubText())
					.template(contents.getTemplate())
					.url(contents.getUrl())
					.build();

				contentsModels.add(contentsModel);
			}

			Optional<Likes> byUserIdAndMagazineId = likesJpaRepository.findByUserIdAndMagazineId(user.getId(),
				magazine.getId());

			MagazineModel magazineModel = MagazineModel.builder()
				.magazineId(magazine.getId())
				.email(user.getEmail())
				.magazineName(magazine.getMagazineName())
				.themeId(magazine.getThemeId())
				.selectedColorId(magazine.getSelectedId())
				.userNickname(user.getNickname())
				.contents(contentsModels)
				.createdDate(LocalDateTime.now())
				.likeCount(magazine.getLikeCount())
				.clicked(byUserIdAndMagazineId.isPresent())
				.build();

			magazineModels.add(magazineModel);
		}

		return magazineModels;
	}

	/**
	 * 인기도 순으로 잡지 조회
	 * @return
	 */
	public List<MagazineModel> getMagazines() {
		List<Magazine> magazineList = magazineRepository.findMagazineOrderByLikes();
		List<MagazineModel> magazineModels = new ArrayList<>();

		for(Magazine magazine : magazineList) {
			List<Contents> contentsList = contentsJpaRepository.findByMagazineId(magazine.getId());
			List<ContentsModel> contentsModelList = new ArrayList<>();

			for(Contents contents : contentsList) {
				ContentsModel contentsModel = ContentsModel.builder()
					.url(contents.getUrl())
					.template(contents.getTemplate())
					.subText(contents.getSubText())
					.question(contents.getQuestion())
					.mainText(contents.getMainText())
					.answer(contents.getAnswer())
					.build();

				contentsModelList.add(contentsModel);
			}

			User user = userJpaRepository.findById(magazine.getUserId()).orElseThrow(CUserNotFoundException::new);
			Optional<Likes> byUserIdAndMagazineId = likesJpaRepository.findByUserIdAndMagazineId(user.getId(),
				magazine.getId());

			MagazineModel magazineModel = MagazineModel.builder()
				.magazineId(magazine.getId())
				.email(user.getEmail())
				.contents(contentsModelList)
				.userNickname(user.getNickname())
				.selectedColorId(magazine.getSelectedId())
				.themeId(magazine.getThemeId())
				.magazineName(magazine.getMagazineName())
				.createdDate(LocalDateTime.now())
				.likeCount(magazine.getLikeCount())
				.clicked(byUserIdAndMagazineId.isPresent())
				.build();

			magazineModels.add(magazineModel);
		}

		return magazineModels;
	}

	public Magazine getMagazine(Long magazineId) {
		return magazineJpaRepository.findById(magazineId).orElseThrow(CCommunicationException::new);
	}

	public LikesModel setLikes(Long magazineId, String userEmail) {
		User user = userJpaRepository.findByEmail(userEmail).orElseThrow(CUserNotFoundException::new);
		Magazine magazine = magazineJpaRepository.findById(magazineId).orElseThrow(CMagazineNotFoundException::new);
		Optional<Likes> likesOptional = likesJpaRepository.findByUserIdAndMagazineId(user.getId(),magazineId);


		logger.info("-------- likes info --------");

		LikesModel likesModel = new LikesModel();
		if(likesOptional.isPresent()) {
			logger.info("라이크 존재");
			likesJpaRepository.deleteById(likesOptional.get().getId());
			likesModel.setClicked(false);
		} else {
			logger.info("라이크 존재하지 않음");
			likesJpaRepository.save(
				Likes.builder()
					.magazineId(magazineId)
					.userId(user.getId())
					.build()
			);
			likesModel.setClicked(true);
		}

		if(likesModel.isClicked()) {
			magazine.setLikeCount(magazine.getLikeCount() + 1);
		} else {
			magazine.setLikeCount(magazine.getLikeCount() - 1);
		}
		magazineJpaRepository.save(magazine);

		likesModel.setMagazineId(magazineId);
		likesModel.setUserId(user.getId());

		logger.info("magazine Id : " + likesModel.getMagazineId());
		logger.info("user Id : " + likesModel.getUserId());

		logger.info("-------- likes info --------");
		return likesModel;
	}

	public List<Magazine> getBookmarkMagazines(String userEmail) {
		User user = userJpaRepository.findByEmail(userEmail).orElseThrow(CUserNotFoundException::new);
		return bookmarkRepository.findBookmarkRepository(user);
	}
}
