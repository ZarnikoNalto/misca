package msifeed.misca;

import msifeed.misca.charsheet.CharsheetConfig;
import msifeed.misca.charstate.CharstateConfig;
import msifeed.misca.chatex.ChatexConfig;
import msifeed.misca.environ.EnvironRule;
import msifeed.misca.locks.LocksConfig;

import java.util.HashMap;
import java.util.Map;

public class MiscaSharedConfig {
    public ChatexConfig chat = new ChatexConfig();
    public Map<Integer, EnvironRule> environ = new HashMap<>();
    public LocksConfig locks = new LocksConfig();
    public CharstateConfig charstate = new CharstateConfig();
    public CharsheetConfig charsheet = new CharsheetConfig();

    public String externalBookUrlTemplate = "https://library.ariadna.su/view.php?id=%s";
}
