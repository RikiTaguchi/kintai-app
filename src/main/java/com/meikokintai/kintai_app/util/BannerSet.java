package com.meikokintai.kintai_app.util;

public abstract class BannerSet {
    
    public static String getBannerMessage (Integer bannerCode) {
        switch (bannerCode) {
            case 0: return "エラーが発生しました。";
            case 1: return "ログインが完了しました。";
            case 2: return "ログアウトが完了しました。";
            case 3: return "未登録のアカウントです。";
            case 4: return "パスワードが違います。";
            case 5: return "登録が完了しました。";
            case 6: return "更新が完了しました。";
            case 7: return "削除が完了しました。";
            case 8: return "管理者によりロックされているため、登録できません。";
            case 9: return "管理者によりロックされているため、更新できません。";
            case 10: return "管理者によりロックされているため、削除できません。";
            case 11: return "入力内容に不備があります。";
            case 12: return "このログインIDは既に使用されているため、登録できません。";
            default: return null;
        }
    }
    
}
