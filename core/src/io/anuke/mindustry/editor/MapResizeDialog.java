package io.anuke.mindustry.editor;

import com.badlogic.gdx.utils.Align;
import io.anuke.mindustry.io.MapTileData;
import io.anuke.mindustry.ui.dialogs.FloatingDialog;
import io.anuke.ucore.function.BiConsumer;
import io.anuke.ucore.scene.ui.ButtonGroup;
import io.anuke.ucore.scene.ui.TextButton;
import io.anuke.ucore.scene.ui.layout.Table;

public class MapResizeDialog extends FloatingDialog{
	int[] validMapSizes = {128, 256, 512};
	int width, height;
	
	public MapResizeDialog(MapEditor editor, BiConsumer<Integer, Integer> cons){
		super("$text.editor.resizemap");
		shown(() -> {
			content().clear();
			MapTileData data = editor.getMap();
			width = data.width();
			height = data.height();
			
			Table table = new Table();
			
			for(int d = 0; d < 2; d ++){
				boolean w = d == 0;
				int curr = d == 0 ? data.width() : data.height();
				int idx = 0;
				for(int i = 0; i < validMapSizes.length; i ++)
					if(validMapSizes[i] == curr) idx = i;
				
				table.add(d == 0 ? "$text.width": "$text.height").padRight(8f);
				ButtonGroup<TextButton> group = new ButtonGroup<>();
				for(int i = 0; i < validMapSizes.length; i ++){
					int size = validMapSizes[i];
					TextButton button = new TextButton(size + "", "toggle");
					button.clicked(() -> {
						if(w)
							width = size;
						else
							height = size;
					});
					group.add(button);
					if(i == idx) button.setChecked(true);
					table.add(button).size(100f, 54f).pad(2f);
				}
				
				table.row();
			}
			
			content().label(() -> 
				width + height > 512 ? "$text.editor.resizebig" : ""
			).get().setAlignment(Align.center, Align.center);
			content().row();
			content().add(table);
			
		});
		
		buttons().defaults().size(200f, 50f);
		buttons().addButton("$text.cancel", this::hide);
		buttons().addButton("$text.editor.resize", () -> {
			cons.accept(width, height);
			hide();
		});
		
	}
}
