package com.itsthwng.twallpaper.remote

import java.util.*

class RemoteConfig {
    companion object {
        const val EU_SERVER_REGION =
            ",al,ad,at,by,be,ba,bg,hr,cy,cz,dk,ee,fo,fi,fr,de,gi,gr,hu,is,ie,im,in,it,rs,lv,li,lt,lu,mk,mt,md,mc,me,nl,no,pl,pt,ro,ru,sm,rs,sk,si,es,se,ch,ua,gb,va,rs,ml,so,ng,ci,uz,au,ye,mr,bf,ly,sn,za"
        const val ASIA_SERVER_REGION =
            ",af,am,az,bh,bd,bt,bn,kh,cx,cc,io,ge,id,ir,iq,il,jo,kz,kw,kg,la,lb,mo,my,mv,mn,mm,np,kp,om,ps,ph,qa,sa,sg,lk,sy,tj,th,tr,tm,ae,vn,"
        const val EAST_ASIA_REGION = ",tw,jp,kr,hk,cn,"
        const val WEST_ASIAN = ",in,"

        val langCountryCode = listOf(
            *"ar_SA,az_AZ,bg_BG,cs_CZ,da_DK,de_DE,el_GR,es_ES,fa_IR,fi_FI,fr_FR,hr_HR,hu_HU,in_ID,it_IT,iw_IL,ja_JP,ko_KR,lt_LT,lv_LV,mr_IN,ms_MY,nl_NL,pl_PL,pt_BR,ro_RO,ru_RU,sk_SK,sr_RS,sv_SE,th_TH,tr_TR,uk_UA,vi_VN,zh_TW,cn_TW,hk_TW".split(
                ",".toRegex()
            ).toTypedArray()
        )

        var countryName = "VN"
        val language: String
            get() = Locale.getDefault().language + "_" + countryName
        var ANDROID_ID = "08A3885D9463AE365B56C859AF40041A"

        fun getRegion(): String {
            return when {
                EU_SERVER_REGION.contains(countryName, ignoreCase = true) -> RegionCode.EU.name
                ASIA_SERVER_REGION.contains(countryName, ignoreCase = true) -> RegionCode.AS.name
                EAST_ASIA_REGION.contains(countryName, ignoreCase = true) -> RegionCode.EA.name
                WEST_ASIAN.contains(countryName, ignoreCase = true) -> RegionCode.WA.name
                else -> RegionCode.US.name
            }
        }
    }

    internal enum class RegionCode {
        US,EU, AS, EA, WA
    }

}