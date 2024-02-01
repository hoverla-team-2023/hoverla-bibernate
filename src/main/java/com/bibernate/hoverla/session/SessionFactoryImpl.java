package com.bibernate.hoverla.session;

import java.sql.Connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionFactoryImpl implements SessionFactory {

  private final Connection connection;

}
