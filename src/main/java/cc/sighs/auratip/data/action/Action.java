package cc.sighs.auratip.data.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public interface Action {
    <T> T accept(ActionVisitor<T> visitor);

    Codec<Action> CODEC = ActionRegistry.codec();

    interface ActionVisitor<T> {

        T visitRunCommand(RunCommand action);

        T visitSimulateKey(SimulateKey action);

        default T visitScript(ScriptAction action) {
            return null;
        }
    }

    record RunCommand(String command) implements Action {
        @Override
        public <T> T accept(ActionVisitor<T> visitor) {
            return visitor.visitRunCommand(this);
        }

        public static final Codec<RunCommand> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("command").forGetter(RunCommand::command)
                ).apply(inst, RunCommand::new)
        );
    }

    record SimulateKey(int keyCode) implements Action {
        @Override
        public <T> T accept(ActionVisitor<T> visitor) {
            return visitor.visitSimulateKey(this);
        }

        public static final Codec<SimulateKey> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("key_code").forGetter(SimulateKey::keyCode)
                ).apply(inst, SimulateKey::new)
        );
    }

    record ScriptAction(String type, Map<String, Dynamic<?>> params) implements Action {
        @Override
        public <T> T accept(ActionVisitor<T> visitor) {
            return visitor.visitScript(this);
        }
    }
}