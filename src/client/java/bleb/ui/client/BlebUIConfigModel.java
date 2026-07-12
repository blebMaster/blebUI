package bleb.ui.client;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;


@Config(name = "blebui-config", wrapperName = "BlebUIConfig")
@Modmenu(modId = "blebui")
public class BlebUIConfigModel {
    @RangeConstraint(min = 1, max = 60)
    public int backgroundFps = 5;
}
