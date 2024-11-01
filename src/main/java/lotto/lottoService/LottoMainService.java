package lotto.lottoService;

import lotto.lottoModel.*;
import lotto.lottoView.LottoPrize;
import lotto.Utility.LottoNumberGenerator;

import java.util.*;

public class LottoMainService {

    private final LottoDAO lottoDAO;
    private final HitLottoDAO hitLottoDAO;
    private final StatisticsLottoDAO statisticsDAO;

    public LottoMainService(LottoDAO lottoDAO, HitLottoDAO hitLottoDAO, StatisticsLottoDAO statisticsDAO) {
        this.lottoDAO = lottoDAO;
        this.hitLottoDAO = hitLottoDAO;
        this.statisticsDAO = statisticsDAO;
    }

    public void buyLotto(long calcCost) {
        long numberOfBuy = calcCost / 1000;

        for (int i = 0; i < numberOfBuy; i++) {
            Lotto lotto = new Lotto(LottoNumberGenerator.generateLottoNumbers());
            lottoDAO.save(lotto);
        }
    }

    // 당첨 번호 저장
    public void saveHitLotto(String hitLottoInput, String bonusNumberInput) {
        List<Integer> hitNumbers = Arrays.stream(hitLottoInput.split(","))
                .map(Integer::parseInt)
                .toList();
        int bonusNumber = Integer.parseInt(bonusNumberInput);
        HitLotto.getInstance(hitNumbers, bonusNumber);
    }

    // 로또 번호와 당첨 번호 비교 및 통계 저장
    public void retainLotto(List<Lotto> allLottos, List<Integer> hitLottos) {
        for (Lotto lotto : allLottos) {
            Set<Integer> lottoNumber = new HashSet<>(lotto.getNumbers());
            Set<Integer> hitLottoNumber = new HashSet<>(hitLottos);
            lottoNumber.retainAll(hitLottoNumber); // 두 세트의 공통 원소만 뽑아서 합친 세트
            saveLottoStatistics(lottoNumber);
        }
    }

    // 통계 저장
    public void saveLottoStatistics(Set<Integer> lottoNumber) {
        int hitSize = lottoNumber.size();
        HitLotto hitLotto = HitLotto.getInstance(null, 0);

        // 3~6까지 맞춘 횟수 빈도 추가
        if (hitSize >= 3 && hitSize <= 6) {
            statisticsDAO.updateSizeFrequency(hitSize);
        }

        // 5일 때 특정 값이 있는지 확인하고 있으면 추가
        if (hitSize == 5 && lottoNumber.contains(hitLotto.getBonusNumber())) {
            statisticsDAO.addSpecificValue();
        }
    }

    // 상금 합계 계산
    public long sumPrize(StatisticsLottoDTO stats) {
        long sumPrize = 0;
        for (int i = 3; i <= 6; i++) {
            if (i == 5) {
                sumPrize += LottoPrize.getPrize(i, false) * (stats.getHitNumberValue(i) - stats.getBonusNumberFrequency());
                sumPrize += LottoPrize.getPrize(i, true) * stats.getBonusNumberFrequency();
            }
            sumPrize += LottoPrize.getPrize(i, false) * stats.getHitNumberValue(i);
        }
        return sumPrize;
    }

}