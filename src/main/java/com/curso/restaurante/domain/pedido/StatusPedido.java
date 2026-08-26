package com.curso.restaurante.domain.pedido;

import java.util.EnumSet;
import java.util.Set;

public enum StatusPedido {

    ABERTO {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.of(EM_PREPARO, CANCELADO);
        }
    },
    EM_PREPARO {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.of(PRONTO, CANCELADO);
        }
    },
    PRONTO {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.of(ENTREGUE, CANCELADO);
        }
    },
    ENTREGUE {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.of(PAGO);
        }
    },
    PAGO {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.noneOf(StatusPedido.class);
        }
    },
    CANCELADO {
        @Override
        public Set<StatusPedido> proximos() {
            return EnumSet.noneOf(StatusPedido.class);
        }
    };

    public abstract Set<StatusPedido> proximos();

    public boolean podeTransicionarPara(StatusPedido destino) {
        return proximos().contains(destino);
    }

    public boolean ehFinal() {
        return proximos().isEmpty();
    }
}
