package cc.sighs.auratip.data.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface Action {
    <T> T accept(ActionVisitor<T> visitor);

    Codec<Action> CODEC = ActionRegistry.codec();

    interface ActionVisitor<T> {
        T visitOpenGui(OpenGui action);

        T visitRunCommand(RunCommand action);

        T visitSimulateKey(SimulateKey action);
    }

    record OpenGui(String classPath) implements Action {
        @Override
        public <T> T accept(ActionVisitor<T> visitor) {
            return visitor.visitOpenGui(this);
        }

        public static final Codec<OpenGui> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("class_path").forGetter(OpenGui::classPath)
                ).apply(inst, OpenGui::new)
        );
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
}