package ayaya.core.enums;

/**
 * The different easter eggs of the rate command.
 */
public enum RateSpecialCases {

    KINIRO_MOSAIC("kiniro mosaic", "The best kind of anime."),
    ALICE("alice cartelet", "Oh, Alice is a nice girl. <:AyaSmile:331115374739324930>"),
    SHINO("shinobu ōmiya", "That mind absent. ^^"),
    SHINO2("ōmiya shinobu", "That mind absent. ^^"),
    SHINO3("shinobu oomiya", "That mind absent. ^^"),
    SHINO4("oomiya shinobu", "That mind absent. ^^"),
    KAREN("karen kujō", "She still keeps accepting food from others."),
    KAREN2("kujō karen", "She still keeps accepting food from others."),
    KAREN3("karen kujou", "She still keeps accepting food from others."),
    KAREN4("kujou karen", "She still keeps accepting food from others."),
    AYA("aya komichi", "Me? I don't know."),
    AYA2("komichi aya", "Me? I don't know."),
    AYAYA("ayaya", "<:AngryAya:331115100771450880> Stop calling me that!"),
    YOKO("yōko inokuma",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>"),
    YOKO2("inokuma yōko",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>"),
    YOKO3("youko inokuma",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>"),
    YOKO4("inokuma youko",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>"),
    YOKO5("yoko inokuma",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>"),
    YOKO6("inokuma yoko",
            "Y-Yoko is my best friend. Just my best friend, you hear?! <:AyaBlush:331115100658204672>");

    private String quote;
    private String answer;

    RateSpecialCases(String quote, String answer) {
        this.quote = quote;
        this.answer = answer;
    }

    public String toString() {
        return quote;
    }

    public String getAnswer() {
        return answer;
    }

}